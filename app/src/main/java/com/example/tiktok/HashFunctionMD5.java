package com.example.tiktok;

import android.util.Log;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

public class HashFunctionMD5 {
    private HashMap<Integer, String> hashBrokersInfo; // <1422306168, 127.0.0.1:4321>
    private TreeMap<Integer, String> hashTreeBrokersInfo; // After hashing, hashes and brokerID's are saved in sorted way.

    public HashFunctionMD5() {
        this.hashBrokersInfo = new HashMap<>();
        this.hashTreeBrokersInfo = new TreeMap<>();
    }

    public static int hashFunction(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] inputByte = md.digest(input.getBytes());

            ByteBuffer wrapped = ByteBuffer.wrap(inputByte);
            int inputByteToInt = wrapped.getInt();

            Log.d("DEBUG:",input + " -> " + Math.abs(inputByteToInt));

            return Math.abs(inputByteToInt);

        } catch (NoSuchAlgorithmException e) {

        }
        return 0;
    }

    public String chooseBroker(int input) {
        Set<Integer> brokersInfoInt = this.hashTreeBrokersInfo.keySet();
        int inp = input % Collections.max(brokersInfoInt);
        Log.d("DEBUG:",inp + "");

        for (int i : brokersInfoInt) {
            Log.d("DEBUG:","Is " + i + " > " + inp + "?");
            if (i > inp) {
                Log.d("DEBUG:",this.hashTreeBrokersInfo.get(i));
                return this.hashTreeBrokersInfo.get(i);
            }
        }
        return null;
    }

    public void hashByteintoInt(ArrayList<String> brokersInfo) {
        for (String str : brokersInfo) {
            this.hashBrokersInfo.put(hashFunction(str), str);
        }

        this.hashTreeBrokersInfo.putAll(hashBrokersInfo);
    }

    public static void main(String[] args) {
        HashFunctionMD5 hash = new HashFunctionMD5();
        ArrayList<String> brokersInfo = new ArrayList<String>();

        brokersInfo.add("127.0.0.1:4321");
        brokersInfo.add("127.0.0.1:4322");
        brokersInfo.add("127.0.0.1:4323");

        hash.hashByteintoInt(brokersInfo);
        String chosenBroker = hash.chooseBroker(hash.hashFunction("Aggelos"));
    }
}
