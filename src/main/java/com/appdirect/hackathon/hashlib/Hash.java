package com.appdirect.hackathon.hashlib;

import com.appdirect.hackathon.notification.NotificationService;
import com.appdirect.hackathon.notification.NotificationServiceImpl;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Hash {

    private static final String SUBJECT = "Important! Error validating chunk data";
    private static final String EMAIL_TEMPLATE = "Hi," +
            "\n" +
            "\nThe validation of the following data chunk is failed : " +
            "\nChunk id : %s" +
            "\n" +
            "\nPlease retry processing for the chunk." +
            "\nThanks! ";

    private static NotificationService notificationService = new NotificationServiceImpl();

    public static String calculate(List<String> ids) throws NoSuchAlgorithmException {
        String collect = ids.stream().sorted().collect(Collectors.joining());

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(collect.getBytes());

        return String.format("%032x", new BigInteger(1, digest));
    }

    public static boolean validate(List<String> ids, String hash) throws NoSuchAlgorithmException {
        String calculatedHash = calculate(ids);
        return calculatedHash.equals(hash);
    }

    /**
     * Validate checksum agonists ids and send email notification in case of failure
     *
     * @param ids
     * @param hash
     * @param chunkId chunkId
     * @param toEmails Comma separated list of email ids to which notification will be send e.g. "raushan.amar@appdirect.com, bhupendra.singh@appdirect.com, ketan.mulay@appdirect.com"
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static boolean validateAndNotify(List<String> ids, String hash, String chunkId, String toEmails) throws NoSuchAlgorithmException {
        boolean result = validate(ids, hash);
        if(!result) {
            notificationService.sendNotification(SUBJECT, String.format(EMAIL_TEMPLATE, chunkId), toEmails);
        }

        return result;
    }


    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.println(calculate(Arrays.asList("abc", "xyz")));
        System.out.println(calculate(Arrays.asList("aba", "xyz")));
        System.out.println(calculate(Arrays.asList("aba")));

        List<String> ids = new ArrayList<>();
        IntStream.range(1, 3000).forEach(i -> ids.add("9b379d66b053026bbac350e08708e1ba37faa0259b0c542f8b66c48727d79400"+i));

        System.out.println(calculate(ids));

        validateAndNotify(ids, "9b379d66b053026bbac350e08708e1ba37faa0259b0c542f8b66c48727d79400", "123", "raushan.amar@appdirect.com");

    }
}
