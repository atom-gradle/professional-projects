package com.qian.utils;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

/**
 * @version 1.0.0
 * @author Qian
 * @since 2.0.0
 * 调用 Thumbnailator , ffmpeg 获取视频时长、缩略图等
 */

@Component
public class FFmpegUtil {

    public void executeFfmpegCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // 可替换为日志记录
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg 执行失败，退出码：" + exitCode);
        }
    }

    public void createVideoThumbnail(String inFileName, String outFileName) throws Exception {
        // 使用 ProcessBuilder 调用 FFmpeg 命令
        ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", inFileName, "-vf", "scale=100:100", "-vframes", "1", outFileName);
        Process process = processBuilder.start();

        // 读取 FFmpeg 输出信息（可选）
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg command execution failed with exit code " + exitCode);
        }
    }

    public void createImageThumbnail(String inFileName, String outFileName) throws IOException {
        Thumbnails.of(new File(inFileName))
                .size(100, 100) // 设置缩略图大小
                .outputFormat("jpg") // 输出格式
                .toFile(new File(outFileName)); // 输出文件路径
    }
}
