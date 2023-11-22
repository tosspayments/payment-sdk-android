package io.fincube.payment;

/**
 * Created by tmxkorea on 2018. 6. 20..
 */


import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class CimageRSACipher {


        PublicKey extPublicKey;
        PrivateKey extPrivateKey;


        public static final String KEY_ALGORITHM = "RSA";
        public static int KEYSIZE = 2048;
        public static int decodeLen = KEYSIZE / 8;
        public static int encodeLen = (KEYSIZE / 8) - 11;
        public static final String ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding";
        public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

        public CimageRSACipher()
                throws NoSuchAlgorithmException,
                NoSuchPaddingException,
                InvalidKeyException,
                InvalidKeySpecException,
                IllegalBlockSizeException,
                BadPaddingException {

            LoadKeyPair(true);
        }

        private void LoadKeyPair(boolean testOff)
        {
            byte[] encodedPub = null;

            if (testOff) {

                String pemPubKeyVP =
                        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4UX5SZW796l3jS+7oE/d" + "\n" +
                                "0dm2LJA3jxs9bWhEyvJB5+v++mmeIoBIQoFV7lwbn0zWMCdZnttXPiN89MsQJwUP" + "\n" +
                                "QESdB3kbbwgm7+70kDFPLv+rxmynRBEsy2NPr9MjmA5Zs8YmcJ33XMiXo8kfePd4" + "\n" +
                                "rLYbmYmWb+uD5w7wGVBDPwj+2iLTUO2/6FWPzj96hwt29TF0nbmyMLmdS7PQI3+e" + "\n" +
                                "JcHj2SOQsEzNwLbuIAvq4Xy09LttIZEwmFjXIYUbBUUbYxuxBKWEP/TeI/7QSzHD" + "\n" +
                                "kDzNR7hH0egAnXTtB1YZmE+HZJWch7aq4zzaoe5RIN/Lb+z+7fAMSit9S9xVq3pt" + "\n" +
                                "0wIDAQAB";
                try {
                    encodedPub = Base64.decode(pemPubKeyVP, Base64.DEFAULT);
                    X509EncodedKeySpec keySpecPub = new X509EncodedKeySpec(encodedPub);
                    KeyFactory kfPub = KeyFactory.getInstance(KEY_ALGORITHM);
                    extPublicKey = kfPub.generatePublic(keySpecPub);
                } catch (NoSuchAlgorithmException |
                        InvalidKeySpecException exception) {
                    Log.e("RSACipherErrorException", exception.getMessage());
                }
            } else {
                String pemPubKeyTest =
                        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzRwqCD2lFYKtUm5d0nld" + "\n" +
                                "KSlVb4jlsUOlS53axUd2r8fTY1wtX4sw/ShIfGDMVm1glGg1FtrYaWr6dz9OQN04" + "\n" +
                                "I2/cGIcTRZvz5Qc9RHxFkFiBWEDlKHQg1rCINnENsXetc2Pb4zRHAEmiM5hx11ly" + "\n" +
                                "Kq2SlnT1OA1Gj7TYrw+bx3sEn8cabw9QENRMwLMWGZUQ6W5ioW0/GpeKyogWnUWB" + "\n" +
                                "Q7dqYuwtopRj2fsMwCaZeYlFqs528C6XoYYgBZFZ2M7DBD++r6eivYcw1u/bKUEb" + "\n" +
                                "aNdG90dPRUBPRL1R8JvOLXxURcC3OvcqhCTzZmcjPdbaWqLRcCNb3G5/R1GMk0+C" + "\n" +
                                "ewIDAQAB";
                try {
                    encodedPub = Base64.decode(pemPubKeyTest, Base64.DEFAULT);
                    X509EncodedKeySpec keySpecPub = new X509EncodedKeySpec(encodedPub);
                    KeyFactory kfPub = KeyFactory.getInstance(KEY_ALGORITHM);
                    extPublicKey = kfPub.generatePublic(keySpecPub);
                } catch (NoSuchAlgorithmException |
                        InvalidKeySpecException exception) {
                    Log.e("RSACipherErrorException", exception.getMessage());
                }
            }
            String pemPrivKeyTest =
            "MIIEpAIBAAKCAQEAzRwqCD2lFYKtUm5d0nldKSlVb4jlsUOlS53axUd2r8fTY1wt"+ "\n" +
            "X4sw/ShIfGDMVm1glGg1FtrYaWr6dz9OQN04I2/cGIcTRZvz5Qc9RHxFkFiBWEDl"+ "\n" +
            "KHQg1rCINnENsXetc2Pb4zRHAEmiM5hx11lyKq2SlnT1OA1Gj7TYrw+bx3sEn8ca"+ "\n" +
            "bw9QENRMwLMWGZUQ6W5ioW0/GpeKyogWnUWBQ7dqYuwtopRj2fsMwCaZeYlFqs52"+ "\n" +
            "8C6XoYYgBZFZ2M7DBD++r6eivYcw1u/bKUEbaNdG90dPRUBPRL1R8JvOLXxURcC3"+ "\n" +
            "OvcqhCTzZmcjPdbaWqLRcCNb3G5/R1GMk0+CewIDAQABAoIBAQCe5nbZs4g1Reuo"+ "\n" +
            "tMAS0tsNjGwX4GKyFcn07vIRa+/S9mgZzcuI/XiSZ6bgVM15F3t+/cxDTH/kC3DG"+ "\n" +
            "+VBSiA95xKsqhVq0zsEAab9ZUz1BOo/aI0xlLrJSIKkmJCeLmT/S6UXsmw92dZUn"+ "\n" +
            "i66GO1FrJtUtoqZnKkHEC0LjPKBexNfervDq8CTlltUDOlvfJFTXaHvf4yjpsIGw"+ "\n" +
            "ptwghzzgTNJgIvYikJsmA4BznVD5usvbu306CH3pfb3tg+v7XCBypTT81RxFvTk/"+ "\n" +
            "CrNW6wNoccogs2mzAd0hiMsXY13QzisYlw582wgmUC0xMl/UoWFnPZbZs4vu+sd6"+ "\n" +
            "b7PquUzhAoGBAO3806nh6QZAD+BYN4deeb4wqWjw4SuGmXcnmnBipEfRn3qQBUr4"+ "\n" +
            "n2FkiaisZFIZHPKeuYS8ZgDRh/yTPnRo114WLm+a5hzDGhEW65Nkjioet0ahg04G"+ "\n" +
            "cYhJtmC4ZSjSPrF52YvraYMOH3kx7OcTKbuUBWgjrVpaml4V0lx7KDo1AoGBANyi"+ "\n" +
            "T8DabiZe2z2/bsFJNrEnBkPEW4v691fxDwZBOmXWzpH0UIReNc17meWvXvIMUj0H"+ "\n" +
            "zzLBGKN35eYTyMYt/o4Zqe0xTdnTK66aV5CMdQNiLhs6NSN6uxeOoaHa8QSSTRXn"+ "\n" +
            "qhAfudamFN5VzFYRdZsh3NnnTakTRVgBZhs/ed/vAoGAfbxDFBEwLUYxXSUeiV2n"+ "\n" +
            "0o0hJDmHrZhagnKhDNmA1BTzPizTLw32Ht2gQUZ7ZkiwW5ryhyeGeM47G32r1JMD"+ "\n" +
            "nkj31a0wAGgv4HGMmv3YgnZh/GYe2l1bJFdVjPo/L/cMenXXMy54O259wGlZMaiV"+ "\n" +
            "Nu54gNHAp+kQf77lukizMzUCgYEAuceygCFtPsIAui3HhNmxQ9ooXEerDmN0goho"+ "\n" +
            "rkuecMI/joa6hbsOcsfw/oZ83DOjy+AXiBUwOfVrkCB4nud7wQu7Gg66DwDRd2Re"+ "\n" +
            "90hN/1tcl3Cb7hQaFev0DeXdrIvEfP1lJc6RHT0btVtpg8+Px+DrplAuBIBFk6UZ"+ "\n" +
            "5Te91+ECgYBTp7UDrSAhHLsmv+HcQ+1RuoDn5Gn/T8xxAszGw0Ze6w962gdRoOzs"+ "\n" +
            "kqwV7pnfUJVX9zLX3hbLohE/OIdVFrO5khlbfpCavkknhe7P9R9JP8UAK91LH5ei"+ "\n" +
            "Hm3YsDn9a2a+C5EOnbq5FNQuSKD5I4mu+BDbOH11b+rGUMNBxK5JwQ==";

            try {
                byte[] encodedPriv = Base64.decode(pemPrivKeyTest, Base64.DEFAULT);
                KeyFactory kfPriv = KeyFactory.getInstance(KEY_ALGORITHM);
                PKCS8EncodedKeySpec keySpecPriv = new PKCS8EncodedKeySpec(encodedPriv);
                extPrivateKey = kfPriv.generatePrivate(keySpecPriv);

            }
                catch (NoSuchAlgorithmException|
                        InvalidKeySpecException exception) {
                    Log.e("RSACipherErrorException", exception.getMessage());
                }

        }


    public void encryptData(String source, String dest)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {

        InputStream input = null;
        OutputStream output = null;

        try {
            input = new BufferedInputStream(new FileInputStream(source));
            output = new BufferedOutputStream(new FileOutputStream(dest));
            byte [] srcData = getBytesFromInputStream(input);
            byte [] encData=encryptPublicKey(srcData);
            output.write(encData);
            output.close();
            input.close();
            //make sure encrypted data with this function as a part of testing ..
            //byte [] decData = null;
            //decData=decryptPrivateKey(encData);
        } catch (Exception  exception) {
            Log.e("Exception", exception.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException exception) {
                    Log.e("IOException", exception.getMessage());
                }
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException exception) {
                        Log.e("IOException", exception.getMessage());
                    }
                }

            }
        }
    }


        private byte[] encryptPublicKey(byte[] encryptedData) throws Exception {
                if (encryptedData == null){
                    throw  new IllegalArgumentException("Input encryption data is null");
                }
                byte[] encode = new byte[] {};
                for (int i = 0; i < encryptedData.length; i += encodeLen) {
                    byte[] subarray = subarray(encryptedData, i, i + encodeLen);
                    byte[] doFinal = encryptByPublicKey(subarray);
                    encode = addAll(encode, doFinal);
                }
                return encode;
            }


        private byte[] decryptPrivateKey(byte[] encode) throws Exception {
                if (encode == null){
                    throw  new IllegalArgumentException("Input data is null");
                }
                byte [] buffers = new byte[]{};
                for (int i = 0; i < encode.length; i += decodeLen) {
                    byte[] subarray = subarray(encode, i, i + decodeLen);
                    byte[] doFinal = decryptByPrivateKey(subarray);
                    buffers = addAll(buffers, doFinal);
                }
                return buffers;
            }



        private byte[] decryptByPrivateKey(byte[] data) throws Exception {
            if (data == null){
                throw  new IllegalArgumentException("Input data is null");
            }

            Cipher cipher = Cipher.getInstance(ECB_PKCS1_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, extPrivateKey);

            return cipher.doFinal(data);
        }


        private byte[] encryptByPublicKey(byte[] data) throws Exception {
            if (data == null){
                throw  new IllegalArgumentException("Input data is null");
            }
            Cipher cipher = Cipher.getInstance(ECB_PKCS1_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, extPublicKey);

            return cipher.doFinal(data);
        }

        private byte[] subarray(final byte[] array, int startIndexInclusive, int endIndexExclusive) {
            if (array == null) {
                return null;
            }
            if (startIndexInclusive < 0) {
                startIndexInclusive = 0;
            }
            if (endIndexExclusive > array.length) {
                endIndexExclusive = array.length;
            }
            final int newSize = endIndexExclusive - startIndexInclusive;
            if (newSize <= 0) {
                return EMPTY_BYTE_ARRAY;
            }

            final byte[] subarray = new byte[newSize];
            System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
            return subarray;
        }


        private byte[] addAll(final byte[] array1, final byte... array2) {
            if (array1 == null) {
                return clone(array2);
            } else if (array2 == null) {
                return clone(array1);
            }
            final byte[] joinedArray = new byte[array1.length + array2.length];
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            return joinedArray;
        }


        private byte[] clone(final byte[] array) {
            if (array == null) {
                return null;
            }
            return array.clone();
        }


        private byte[] getBytesFromInputStream(InputStream inputStream){
            byte[] buffer = null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            try {
                int n;
                while ((n = inputStream.read(b)) != -1) {
                    bos.write(b, 0, n);
                }
                buffer = bos.toByteArray();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return buffer;
        }

}
