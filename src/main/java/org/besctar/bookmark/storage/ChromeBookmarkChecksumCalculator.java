package org.besctar.bookmark.storage;

import org.besctar.bookmark.model.Bookmark;
import org.besctar.bookmark.model.Bookmarks;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChromeBookmarkChecksumCalculator {

    private MessageDigest md5() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String regenChecksum(Bookmarks bookmarks) {
        MessageDigest digest = md5();
        recursiveDigest(bookmarks.getRoots().get("bookmark_bar"), digest);
        recursiveDigest(bookmarks.getRoots().get("other"), digest);
        recursiveDigest(bookmarks.getRoots().get("synced"), digest);
        return byteArrayToHex(digest.digest());
    }

    private void recursiveDigest(Bookmark bookmark, MessageDigest digest) {
        digest.update(bookmark.getId().getBytes(StandardCharsets.US_ASCII));
        digest.update(bookmark.getName().getBytes(StandardCharsets.UTF_16LE));
        if (bookmark.isDirectory()) {
            digest.update("folder".getBytes(StandardCharsets.UTF_8));
            for (Bookmark child : bookmark.getChildren()) {
                recursiveDigest(child, digest);
            }
        } else if (bookmark.isBookmark()) {
            digest.update("url".getBytes(StandardCharsets.UTF_8));
            digest.update(String.valueOf(bookmark.getUrl()).getBytes(StandardCharsets.UTF_8));
        } else {
            throw new IllegalArgumentException("No such bookmark type: " + bookmark.getType());
        }
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
