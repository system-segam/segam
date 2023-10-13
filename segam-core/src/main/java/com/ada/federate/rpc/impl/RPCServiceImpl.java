package com.ada.federate.rpc.impl;

import com.ada.federate.cache.ConcurrentBuffer;
import com.ada.federate.cache.FullKeyListMap;
import com.ada.federate.cache.ResultKVSet;
import com.ada.federate.cache.ResultKVsSet;
import com.ada.federate.rpc.RPCCommon;
import com.ada.federate.rpc.RPCService;
import com.ada.federate.rpc.SegamGrpc;
import com.ada.federate.secure.SecureGroupSum;
import com.ada.federate.secure.SecureUnion;
import com.ada.federate.utils.*;
import io.grpc.stub.StreamObserver;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// import static com.ada.federate.TestApplication.timeframeList;

public abstract class RPCServiceImpl extends SegamGrpc.SegamImplBase {
    protected ConcurrentBuffer buffer = new ConcurrentBuffer();

    protected long startTime, endTime;

    protected FullKeyListMap fullKeyListMap = new FullKeyListMap();

    protected RPCInterface nextClient = null;
    protected List<RPCInterface> clientList = new ArrayList<>();
    protected int siloId;
    protected int siloSize;

    // thread pool for secret sharing
    public static ExecutorService executorService;

    @Override
    public void rpcHello(RPCService.HelloRequest request, StreamObserver<RPCCommon.Status> responseObserver) {
        RPCCommon.Status status = RPCCommon.Status.newBuilder().setCode(true).build();
        RPCService.HelloRequest.Builder requestBuilder = request.toBuilder().clone();
        // long startTime, endTime;
        // startTime = System.currentTimeMillis();
        try {
            List<String> endpointList = request.getEndpointList();
            siloId = request.getSiloId();
            requestBuilder.setSiloId(siloId + 1);
            siloSize = endpointList.size();
            for (int i = 0; i < siloSize; i++) {
                clientList.add(new RPCInterface(endpointList.get(i), i));
            }
            LogUtils.debug(String.format("RPC hello: total [%d] clients, I'm no [%d]", siloSize, siloId));
            List<RPCService.Column> columnList = request.getColumnList();

            if (CollectionUtils.isNotEmpty(columnList)) {
                // List<List<String>> tempList = new ArrayList<>();
                for (RPCService.Column column : columnList) {
                    // tempList.add(column.getFullKeyListList());
                    fullKeyListMap.addKeyList(column.getFieldName(), column.getFullKeyListList());
                }

                // fullKeyListMap = new FullKeyListMap(tempList);
                // if (LogUtils.DEBUG) LogUtils.debug(fullKeyPermutationList.size());
            }
            nextClient = clientList.get((siloId + 1) % siloSize);
            executorService = Executors.newFixedThreadPool(siloSize + 1);
            int nextIndex = (request.getIndex() + 1);
            if (nextIndex == siloSize) requestBuilder.setRound(request.getRound() + 1);
            nextIndex = nextIndex % siloSize;
            if (requestBuilder.getRound() <= 1 && nextIndex < siloSize) {
                if (LogUtils.DEBUG) LogUtils.debug(String.format("rpc hello: next Client (%s), siloId (%s)", nextClient.endpoints, siloId));
                RPCCommon.Status nextStatus = nextClient.rpcHello(requestBuilder.setIndex(nextIndex).build());
                status = status.toBuilder().setCode(status.getCode() && nextStatus.getCode()).build();
            }
        } catch (Exception e) {
            status = status.toBuilder().setCode(false).build();
            LogUtils.error(LogUtils.buildErrorMessage(e));
        } finally {
            // endTime = System.currentTimeMillis();
            // timeframeList.put("hello", endTime - startTime);
            responseObserver.onNext(status);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void command(RPCService.CommandRequest request, StreamObserver<RPCService.CommandResponse> responseObserver) {
        RPCService.CommandResponse.Builder responseBuilder = RPCService.CommandResponse.newBuilder();
        RPCCommon.Status status = RPCCommon.Status.newBuilder().setCode(true).build();
        try {
            if (request.getCommandCode() == 0) {
                cleanBuffer(request.getUuid());
                if (LogUtils.DEBUG) LogUtils.debug("clean buffer finish.");
            }
        } catch (Exception e) {
            status = status.toBuilder().setCode(false).build();
            LogUtils.error(LogUtils.buildErrorMessage(e));
        } finally {
            responseObserver.onNext(responseBuilder.setStatus(status).build());
            responseObserver.onCompleted();
        }
        // super.command(request, responseObserver);
    }

    private void cleanBuffer(Long uuid) {
        buffer.clean(uuid);
    }

    @Override
    public void privateSetUnion(RPCService.SetUnionRequest request, StreamObserver<RPCService.SetUnionResponse> responseObserver) {
        // RPCCommon.Status status = RPCCommon.Status.newBuilder().setCode(true).build();
        RPCCommon.Status.Builder statusBuilder = RPCCommon.Status.newBuilder().setCode(true);
        RPCService.SetUnionRequest.Builder requestBuilder = request.toBuilder().clone();
        RPCService.SetUnionResponse.Builder responseBuilder = RPCService.SetUnionResponse.newBuilder();
        try {
            if (request.getRound() == 1) {
                // startTime = System.currentTimeMillis();
                Set<String> localKeyIndexList = buffer.localResultTable == null ? buffer.localResultKeyValuesTable.keySet() : buffer.localResultTable.keySet();
                // LogUtils.debug(localKeyList);
                // List<Integer> localKeyIndexList = fullKeyListMap1.string2Index(localKeyList);
                if (LogUtils.DEBUG) LogUtils.debug("1.0 full key set:\n " + fullKeyListMap.columnNameList);
                if (LogUtils.DEBUG) LogUtils.debug("1.1 local key set:\n " + localKeyIndexList);
                buffer.unionCache = new SecureUnion.SecureUnionCache(localKeyIndexList);
                List<String> confusionSet = buffer.unionCache.getConfusionSet(fullKeyListMap);
                if (LogUtils.DEBUG) LogUtils.debug("1.2 confusion key set:\n " + confusionSet);
                requestBuilder.addAllKeyIndex(confusionSet);
                // endTime = System.currentTimeMillis();
                // timeframeList.put("1.1 PSU Add", endTime - startTime);
            } else if (request.getRound() == 2) {
                // startTime = System.currentTimeMillis();
                LinkedList<String> keyList = new LinkedList<>(request.getKeyIndexList());
                if (LogUtils.DEBUG) LogUtils.debug("2.1 global key set:\n " + keyList);
                Iterator<String> iterator = keyList.iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    if (buffer.unionCache.localRandomSetPop(key)) {
                        iterator.remove();
                    }
                }
                // endTime = System.currentTimeMillis();
                // timeframeList.put("1.2 PSU Remove", endTime - startTime);
                if (LogUtils.DEBUG) LogUtils.debug("2.2 global key set after removal:\n " + keyList.size());
                if (request.getIndex() == siloSize - 1) {
                    // startTime = System.currentTimeMillis();
                    Set<String> uniqueSet = new LinkedHashSet<>(keyList);
                    // if (LogUtils.DEBUG)
                    LogUtils.debug(String.format("2.3 original list size: [%s], after remove duplicates size: [%s]\n ", keyList.size(), uniqueSet.size()));
                    responseBuilder.addAllKeyIndex(uniqueSet);
                    responseBuilder.setStatus(statusBuilder.build());
                    // endTime = System.currentTimeMillis();
                    // timeframeList.put("1.3 PSU Buffer", endTime - startTime);

                } else {
                    requestBuilder.clearKeyIndex();
                    requestBuilder.addAllKeyIndex(keyList);
                }

            }
            int nextIndex = (request.getIndex() + 1);
            // LogUtils.debug(String.valueOf(nextIndex));
            if (nextIndex == siloSize) requestBuilder.setRound(request.getRound() + 1);
            nextIndex = nextIndex % siloSize;
            if (requestBuilder.getRound() <= 2 && nextIndex < siloSize) {
                RPCService.SetUnionRequest unionRequest = requestBuilder.setIndex(nextIndex).build();
                RPCService.SetUnionResponse nextResponse = nextClient.privateSetUnion(unionRequest);
                responseBuilder.setStatus(nextResponse.getStatus()).addAllKeyIndex(nextResponse.getKeyIndexList());
                // if (LogUtils.DEBUG) LogUtils.debug(nextResponse.getKeyList().toString());
            }
        } catch (Exception e) {
            statusBuilder.setCode(false).setMessage(LogUtils.buildErrorMessage(e));
            responseBuilder.setStatus(statusBuilder.build());
            LogUtils.error(LogUtils.buildErrorMessage(e));
        } finally {
            RPCService.SetUnionResponse build = responseBuilder.build();
            responseObserver.onNext(build);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void secretSharing(RPCCommon.SSRequest ssRequest, StreamObserver<RPCCommon.SSMessage> responseObserver) {
        RPCCommon.SSMessage.Builder responseBuilder = RPCCommon.SSMessage.newBuilder();
        try {
            List<RPCCommon.SSMessage> ssMessageList = buffer.sharingCache.get(ssRequest.getRounds());
            // LogUtils.debug(ssRequest.getRounds());
            if (CollectionUtils.isNotEmpty(ssMessageList)) {
                // intermediateBufferMap.get(ssRequest.getUuid()).secretSharingCount++;
                responseBuilder
                        .addAllShareVal(ssMessageList.get(ssRequest.getIndex()).getShareValList())
                        .setFrom(siloId)
                        .setStatus(RPCCommon.Status.newBuilder().setCode(true));

                // if (LogUtils.DEBUG)
                if (LogUtils.DEBUG)
                    LogUtils.debug(String.format("[**] send %s encrypt value to silo %d", responseBuilder.getShareValList(),
                            ssRequest.getIndex() + 1));
                // intermediateBufferMap.get(ssRequest.getUuid()).secretSharingCount++;
                // if (intermediateBufferMap.get(ssRequest.getUuid()).secretSharingCount == siloSize) {
                //     intermediateBufferMap.get(ssRequest.getUuid()).secretSharingCount = 0;
                //     intermediateBufferMap.get(ssRequest.getUuid()).ssMessageList.clear();
                // }
            } else {
                responseBuilder.setStatus(RPCCommon.Status.newBuilder().setCode(false)).setFrom(siloId);
            }

        } catch (Exception e) {
            // responseBuilder.setCode(false).setMessage(LogUtils.buildErrorMessage(e));
            LogUtils.error(LogUtils.buildErrorMessage(e));
        } finally {
            RPCCommon.SSMessage build = responseBuilder.build();
            responseObserver.onNext(build);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void secureGroupBySumSS(RPCService.GroupBySumSSRequest request, StreamObserver<RPCCommon.SSMessage> responseObserver) {
        RPCCommon.SSMessage.Builder responseBuilder = RPCCommon.SSMessage.newBuilder();

        ResultKVSet resultTable;
        try {
            int rounds = request.getRound();
            // System.out.println(StringUtils.repeat("=", 30) + "rounds " + rounds + StringUtils.repeat("=", 30));
            // LogUtils.debug("start doing secret sharing");
            // sum
            if (request.getQueryType() == 0) {
                // startTime = System.currentTimeMillis();
                resultTable = buffer.localResultTable;
                // if (LogUtils.DEBUG) LogUtils.debug(String.format("1.1 record list size: [%s]", resultTable.rowNumber));
                ResultKVSet fillResultKVSet = new ResultKVSet();
                // LogUtils.debug("123" + fullKeyPermutationList);
                List<String> fullKeyList = request.getKeyListList();
                // if (LogUtils.DEBUG) LogUtils.debug(fullKeyList.size());
                if (fullKeyList.size() != 0) {
                    for (String key : fullKeyList) {
                        fillResultKVSet.addRow(key, resultTable.getOrDefault(key, 0));
                    }
                    resultTable = fillResultKVSet;
                }
                // endTime = System.currentTimeMillis();
                // timeframeList.put("2.1 Sum Fill", endTime - startTime);
                // if (LogUtils.DEBUG) LogUtils.debug(String.format("1.2 record list after fill size: [%s]", resultTable.rowNumber));
            } else {
                // max
                resultTable = buffer.sharingCache.signList;
                // if (LogUtils.DEBUG) LogUtils.debug(String.format("1.1 sign list: \n [%s]", resultTable.keySet().size()));
            }
            // startTime = System.currentTimeMillis();
            // 本地加密
            List<RPCCommon.SSMessage> ssMessageList = SecureGroupSum.localEncrypt(resultTable, request.getPublicKeyList(),
                    siloSize);

            // if (LogUtils.DEBUG) LogUtils.debug("2.1 local encrypt result size: " + ssMessageList.size());
            // intermediateBuffer.ssMessageList.addAll(ssMessageList);
            buffer.sharingCache.add(ssMessageList);
            // endTime = System.currentTimeMillis();

            // LogUtils.debug(String.format("2.2 Sum Encrypt:%d", endTime - startTime));

            // if (LogUtils.DEBUG) LogUtils.debug("3.1 doing secret sharing");

            // startTime = System.currentTimeMillis();

            List<RPCCommon.SSMessage> encryptSSMessageList = new ArrayList<>();
            boolean[] booleanList = new boolean[siloSize];
            Arrays.fill(booleanList, false);
            // booleanList[siloId] = true;
            do {
                // 秘密共享
                List<Callable<RPCCommon.SSMessage>> tasks = new ArrayList<>();
                for (RPCInterface client : clientList) {
                    // if (LogUtils.DEBUG) LogUtils.debug(Arrays.toString(booleanList));
                    if (booleanList[client.siloId])
                        continue;
                    tasks.add(() -> client.secretSharing(RPCCommon.SSRequest.newBuilder()
                            .setUuid(request.getUuid())
                            .setRounds(request.getRound())
                            .setIndex(siloId).build()));
                }

                List<RPCCommon.SSMessage> resultList = ThreadTools.runCallableTasks(executorService, tasks);

                for (RPCCommon.SSMessage ssMessage : resultList) {
                    if (ssMessage.getStatus().getCode()) {
                        encryptSSMessageList.add(ssMessage);
                        booleanList[ssMessage.getFrom()] = true;
                    }
                }
                // check whether each silo has completed secret sharing.
            } while (!ArrayUtils.checkBoolArray(booleanList));
            // endTime = System.currentTimeMillis();
            // LogUtils.debug(String.format("2.3 Sum Sharing:%d", endTime - startTime));
            // startTime = System.currentTimeMillis();

            // if (LogUtils.DEBUG) {
            //     LogUtils.debug("3.3 add secret sharing sum ");
            //     LogUtils.debug(ResultsUtils.flatList(encryptSSMessageList, RPCCommon.SSMessage.class));
            // }
            responseBuilder.addAllShareVal(SecureGroupSum.mergeSSMessage(encryptSSMessageList));

            // endTime = System.currentTimeMillis();
            // LogUtils.debug(String.format("2.4 Sum Add up:%d", endTime - startTime));
            // if (LogUtils.DEBUG) LogUtils.debug(responseBuilder.getShareValList().toString());
            // System.out.println(StringUtils.repeat("=", 30) + "rounds " + rounds + " end" + StringUtils.repeat("=", 30));
        } catch (Exception e) {
            responseBuilder.setStatus(RPCCommon.Status.newBuilder().setCode(false).setMessage(LogUtils.buildErrorMessage(e)).build());
            LogUtils.error(LogUtils.buildErrorMessage(e));
        } finally {
            // Tools.formatPrintTestInfo(timeframeList);
            responseBuilder.setStatus(
                    RPCCommon.Status.newBuilder().setCode(true).build()).build();
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        }
    }

    private final Random random = new Random();

    @Override
    public void secureGroupByMedian(RPCService.GroupByMedianRequest request, StreamObserver<RPCCommon.Status> responseObserver) {
        RPCCommon.Status.Builder responseBuilder = RPCCommon.Status.newBuilder();
        try {
            long startTime, endTime;
            startTime = System.currentTimeMillis();
            ResultKVsSet resultKVSet = buffer.localResultKeyValuesTable;
            buffer.sharingCache.signList.clean();
            List<Boolean> runEvenList = request.getEvenFlagList();
            for (int i = 0; i < request.getKeyIndexCount(); i++) {
                String key = request.getKeyIndex(i);
                if (!resultKVSet.contains(key)) {
                    buffer.sharingCache.signList.updateRow(key, 0);
                } else {
                    int l_count = 0, r_count = 0;
                    if (!runEvenList.get(i)) {
                        for (long val : resultKVSet.get(key)) {
                            if (val >= request.getThreshold(i))
                                r_count++;
                            else
                                l_count++;
                        }
                    } else {
                        for (long val : resultKVSet.get(key)) {
                            if (val <= request.getThreshold(i))
                                l_count++;
                            else
                                r_count++;
                        }
                    }
                    buffer.sharingCache.signList.updateRow(key, l_count - r_count);
                }
            }
            endTime = System.currentTimeMillis();
            // TestApplication.timeframeList.put("Core check threshold", endTime - startTime);
            // LogUtils.debug(buffer.sharingCache.signList);
        } catch (Exception e) {
            responseBuilder.setCode(false).setMessage(LogUtils.buildErrorMessage(e));
            LogUtils.error(LogUtils.buildErrorMessage(e));
        } finally {
            responseObserver.onNext(responseBuilder.setCode(true).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void secureGroupByMax(RPCService.GroupByMaxRequest request, StreamObserver<RPCCommon.Status> responseObserver) {
        RPCCommon.Status.Builder responseBuilder = RPCCommon.Status.newBuilder();
        try {
            ResultKVSet resultKVSet = buffer.localResultTable;
            // LogUtils.debug();
            for (int i = 0; i < request.getKeyIndexCount(); i++) {
                String key = request.getKeyIndex(i);
                if (!resultKVSet.contains(key)) {
                    buffer.sharingCache.signList.updateRow(key, 0);
                } else {
                    int sign = resultKVSet.get(key) >= request.getThreshold(i) ? random.nextInt(10) + 1 : 0;
                    buffer.sharingCache.signList.updateRow(key, sign);
                }
            }
        } catch (Exception e) {
            responseBuilder.setCode(false).setMessage(LogUtils.buildErrorMessage(e));
            LogUtils.error(LogUtils.buildErrorMessage(e));
        } finally {
            responseObserver.onNext(responseBuilder.setCode(true).build());
            responseObserver.onCompleted();
        }
    }
}
