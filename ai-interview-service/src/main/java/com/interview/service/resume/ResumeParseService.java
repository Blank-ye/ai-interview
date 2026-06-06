package com.interview.service.resume;

import com.interview.dao.entity.Resume;
import com.interview.dao.mapper.ResumeMapper;
import com.interview.service.ai.AiClientService;
import com.interview.service.ai.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 简历解析服务
 * 流程：上传文件 -> 提取文本 -> AI结构化解析 -> 向量化存储
 * 支持PDF和Word格式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeParseService {

    private final ResumeMapper resumeMapper;
    private final AiClientService aiClientService;
    private final PromptTemplate promptTemplate;
    private final ResumeVectorService resumeVectorService;

    // 使用用户目录下的固定路径，避免Tomcat临时目录问题
    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "ai-interview" + File.separator + "uploads" + File.separator + "resumes";

    /**
     * 上传简历
     */
    public Resume uploadResume(Long userId, MultipartFile file) {
        // 1. 保存文件
        String fileName = UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
        String filePath = saveFile(file, fileName);

        // 2. 创建简历记录
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setFileName(file.getOriginalFilename());
        resume.setFilePath(filePath);
        resume.setStatus(0); // 待解析
        resume.setCreateTime(LocalDateTime.now());
        resumeMapper.insert(resume);

        return resume;
    }

    /**
     * 解析简历
     */
    public void parseResume(Long resumeId) {
        Resume resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }

        try {
            // 1. 提取文本
            String rawText = extractText(resume.getFilePath());
            resume.setRawContent(rawText);

            // 2. AI结构化解析
            String prompt = promptTemplate.buildResumeParsePrompt(rawText);
            String structuredJson = aiClientService.chat(prompt);
            resume.setStructuredContent(structuredJson);

            // 3. 向量化存储
            String vectorId = resumeVectorService.vectorizeAndStore(resumeId, rawText);
            resume.setVectorId(vectorId);

            // 4. 更新状态
            resume.setStatus(1); // 已解析
            resume.setUpdateTime(LocalDateTime.now());
            resumeMapper.updateById(resume);

            log.info("简历解析成功，resumeId: {}", resumeId);
        } catch (Exception e) {
            log.error("简历解析失败，resumeId: {}", resumeId, e);
            resume.setStatus(2); // 解析失败
            resume.setUpdateTime(LocalDateTime.now());
            resumeMapper.updateById(resume);
            throw new RuntimeException("简历解析失败", e);
        }
    }

    /**
     * 提取文本内容
     */
    private String extractText(String filePath) {
        String extension = getFileExtension(filePath).toLowerCase();
        try {
            if (extension.equals(".pdf")) {
                return extractPdfText(filePath);
            } else if (extension.equals(".docx") || extension.equals(".doc")) {
                return extractWordText(filePath);
            } else {
                throw new RuntimeException("不支持的文件格式：" + extension);
            }
        } catch (IOException e) {
            throw new RuntimeException("文件读取失败", e);
        }
    }

    /**
     * 提取PDF文本
     */
    private String extractPdfText(String filePath) throws IOException {
        try (var document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 提取Word文本
     */
    private String extractWordText(String filePath) throws IOException {
        try (InputStream is = Files.newInputStream(Paths.get(filePath));
             XWPFDocument document = new XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                sb.append(paragraph.getText()).append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * 保存文件
     */
    private String saveFile(MultipartFile file, String fileName) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot) : "";
    }
}
