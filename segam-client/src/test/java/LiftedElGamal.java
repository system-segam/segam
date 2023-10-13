import com.google.common.math.BigIntegerMath;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

//https://blog.csdn.net/u010536377/article/details/44893387

public class LiftedElGamal {
    static class PublicKey {
        private BigInteger p, g;

        public PublicKey(BigInteger p, BigInteger g) {
            this.p = p;
            this.g = g;
        }

        public BigInteger getP() {
            return p;
        }

        public void setP(BigInteger p) {
            this.p = p;
        }

        public BigInteger getG() {
            return g;
        }

        public void setG(BigInteger g) {
            this.g = g;
        }
    }

    static class Ciphertext {
        public BigInteger c1, c2;

        public Ciphertext(BigInteger c1, BigInteger c2) {
            this.c1 = c1;
            this.c2 = c2;

        }

        @Override
        public String toString() {
            return "Ciphertext{" + "c1=" + c1 + ", c2=" + c2 + '}';
        }

        public void multiply(Ciphertext c) {
            this.c1 = this.c1.multiply(c.c1);
            this.c2 = this.c2.multiply(c.c2);
        }
    }

    /**
     * 计算公钥
     */
    public static BigInteger calculateY(List<BigInteger> privateKeyList, BigInteger g, BigInteger p) {
        // 4. 计算公钥 y = g^a (mod p)，并将y公开。
        BigInteger y = BigInteger.ONE;
        BigInteger exp = BigInteger.ZERO;
        for (BigInteger a : privateKeyList) {
            //y = y.multiply(g.modPow(a, p));
            exp = exp.add(a);
        }
        return g.modPow(exp, p);
    }

    /**
     * 生成大素数与原根
     */
    public static PublicKey generateRandomP(int bigLength) {
        Random r = new Random();
        BigInteger p = null, g = null, q = null;

        while (true) {
            q = BigInteger.probablePrime(bigLength, r);
            // if (q.bitLength() != bigLength) continue;
            if (q.isProbablePrime(10)) {
                // 1. 随机地选择一个大素数 p，且要求 p-1 有大素数因子，将 p 公开。
                p = q.multiply(new BigInteger("2")).add(BigInteger.ONE);
                if (p.isProbablePrime(10)) // 如果P为素数则OK~，否则继续
                    break;
            }
        }
        while (true) {
            // 2.选择一个模 p 的原根 g，并将 g 公开。
            g = BigInteger.probablePrime(p.bitLength() - 1, r);
            if (!g.modPow(BigInteger.ONE, p).equals(BigInteger.ONE) && !g.modPow(q, p).equals(BigInteger.ONE)) {
                break;
            }
        }
        return new PublicKey(p, g);
        //return new PublicKey(BigInteger.valueOf(5), BigInteger.valueOf(3));
    }

    /**
     * 生成私钥
     */
    public static BigInteger getRandomA(BigInteger p) {
        // 3.随机地选择一个整数 a（1 ＜ a ＜ p-1）作为私钥，并对 a 保密。
        BigInteger a;
        Random r = new Random();
        a = new BigInteger(p.bitLength() - 1, r);
        return a;
    }


    /***
     * 加密
     * @param m 明文
     * @param y 公钥
     * @param p 大素数
     * @param g 生成元
     */
    public static Ciphertext encrypt(int m, BigInteger y, BigInteger p, BigInteger g) {
        BigInteger C1, C2;
        Random rng = new Random();
        BigInteger r;
        while (true) {
            //System.out.println(1);
            r = new BigInteger(p.bitLength() - 1, rng); // 产生 0<= <p-1的随机数
            //r = BigInteger.valueOf(3);
            // 如果随机数与p-1互质
            if (r.gcd(p.subtract(BigInteger.ONE)).equals(BigInteger.ONE)) {
                // 则选取成功,返回随机数k
                break;
            }
        }
        System.out.println("加密随机数 r: " + r);
        // 计算密文 C1,C2
        C1 = g.modPow(r, p);
        C2 = BigInteger.valueOf(2).modPow(BigInteger.valueOf(m), p).multiply(y.modPow(r, p)).mod(p);
        return new Ciphertext(C1, C2);
    }

    /***
     * 解密
     * @param ciphertext 密文
     * @param A 私钥
     * @param p 大素数
     * @return
     */
    public static int decrypt(Ciphertext ciphertext, List<BigInteger> A, BigInteger p) {
        BigInteger C1 = ciphertext.c1, C2 = ciphertext.c2;
        BigInteger t = BigInteger.ONE;
        for (BigInteger a : A) {
            t = t.multiply(C1.pow(a.intValue()));
        }
        // 2^m=power
        BigInteger power = C2.multiply(t.modPow(BigInteger.valueOf(-1), p)).mod(p);
        int upperBound = BigIntegerMath.log2(p, RoundingMode.FLOOR);
        System.out.println(power);
        for (int i = 0; i < upperBound; i++) {
            if (BigIntegerMath.isPowerOfTwo(power))
                break;
            else
                power = power.add(p);
        }
        System.out.println(power);
        return BigIntegerMath.log2(power, RoundingMode.CEILING);
    }

    public static void main(String[] args) {
        BigInteger h, a;
        List<Integer> M = Arrays.asList(3, 3, 2, 2);

        PublicKey PG = LiftedElGamal.generateRandomP(100);
        BigInteger p = PG.getP(), g = PG.getG();
        // 随机生成私钥
        List<BigInteger> privateKeyList = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            //privateKeyList.add(LiftedElGamal.getRandomA(p));
            privateKeyList.add(BigInteger.valueOf(i + 1));
        }
        // 联合生成公钥
        h = LiftedElGamal.calculateY(privateKeyList, g, p);

        System.out.println("随机生成素数P: " + p + ", 求得其生成元: " + g);
        System.out.println("私钥<a>为: (" + privateKeyList.stream().map(Objects::toString).collect(Collectors.toList()));
        System.out.println("公钥<h,g,p>为:" + "(" + h + "," + g + "," + p + ")");

        List<Ciphertext> C = new ArrayList<>();
        // 加密
        for (Integer m : M) {
            Ciphertext c = LiftedElGamal.encrypt(m, h, p, g);
            C.add(c);
        }
        System.out.println("加密后的密文为:" + C.stream().map(Object::toString).collect(Collectors.toList()));
        //System.out.println("解密密文得到:" + decrypt(C.get(0), privateKeyList, p) + " " + decrypt(C.get(1), privateKeyList, p));
        // 密文相乘
        //BigInteger cipherRes = BigInteger.ONE;
        Ciphertext cipherProduct = new Ciphertext(BigInteger.ONE, BigInteger.ONE);
        for (Ciphertext c : C) {
            cipherProduct.multiply(c);
        }
        System.out.println("密文相乘结果为:" + cipherProduct);
        int decM = LiftedElGamal.decrypt(cipherProduct, privateKeyList, p);
        System.out.println("解密得到明文为:" + decM);
    }
}