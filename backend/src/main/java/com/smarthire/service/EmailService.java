package com.smarthire.service;

import com.smarthire.model.Interview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Email Service for sending interview notifications
 * This is a basic implementation that logs emails
 * In production, integrate with SendGrid, AWS SES, or similar
 */
@Service
@Slf4j
public class EmailService {
    
    @Value("${app.company.name:AutoHireX}")
    private String companyName;
    
    @Value("${app.company.email:noreply@autohirex.com}")
    private String companyEmail;
    
    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    
    /**
     * Send interview invitation email
     */
    public boolean sendInterviewInvitation(Interview interview, String interviewType) {
        try {
            log.info("📧 Sending interview invitation to: {}", interview.getCandidateEmail());
            
            String emailBody = buildInterviewInvitationEmail(interview, interviewType);
            String subject = buildEmailSubject(interview, interviewType);
            
            // Log email (in production, send via actual email service)
            log.info("=================================");
            log.info("FROM: {}", companyEmail);
            log.info("TO: {}", interview.getCandidateEmail());
            log.info("SUBJECT: {}", subject);
            log.info("BODY:\n{}", emailBody);
            log.info("=================================");
            
            // TODO: Integrate with actual email service
            // Example with SendGrid, AWS SES, or JavaMail
            // sendEmailViaSMTP(interview.getCandidateEmail(), subject, emailBody);
            
            return true;
            
        } catch (Exception e) {
            log.error("❌ Error sending interview invitation: ", e);
            return false;
        }
    }
    
    /**
     * Build email subject
     */
    private String buildEmailSubject(Interview interview, String interviewType) {
        if ("VOICE_AI".equals(interviewType)) {
            return String.format("🎯 AI Interview Invitation - %s at %s", 
                interview.getJobTitle(), companyName);
        } else {
            return String.format("📅 Interview Invitation - %s at %s", 
                interview.getJobTitle(), companyName);
        }
    }
    
    /**
     * Build interview invitation email body
     */
    private String buildInterviewInvitationEmail(Interview interview, String interviewType) {
        StringBuilder email = new StringBuilder();
        
        email.append("<!DOCTYPE html>");
        email.append("<html>");
        email.append("<head>");
        email.append("<style>");
        email.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        email.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        email.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }");
        email.append(".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }");
        email.append(".info-box { background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #667eea; border-radius: 4px; }");
        email.append(".button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }");
        email.append(".rounds { background: #fff3e0; padding: 15px; border-radius: 6px; margin: 15px 0; }");
        email.append(".footer { text-align: center; color: #666; margin-top: 30px; font-size: 12px; }");
        email.append("</style>");
        email.append("</head>");
        email.append("<body>");
        
        email.append("<div class='container'>");
        email.append("<div class='header'>");
        email.append("<h1>🎉 Interview Invitation</h1>");
        email.append("<p>You've been shortlisted for the next round!</p>");
        email.append("</div>");
        
        email.append("<div class='content'>");
        email.append("<h2>Dear ").append(interview.getCandidateName()).append(",</h2>");
        email.append("<p>Congratulations! We are pleased to inform you that you have been shortlisted for an interview for the <strong>")
            .append(interview.getJobTitle()).append("</strong> position at <strong>").append(companyName).append("</strong>.</p>");
        
        // Interview Details
        email.append("<div class='info-box'>");
        email.append("<h3>📋 Interview Details</h3>");
        email.append("<p><strong>Position:</strong> ").append(interview.getJobTitle()).append("</p>");
        email.append("<p><strong>Date:</strong> ").append(interview.getScheduledDate().format(DATE_FORMATTER)).append("</p>");
        email.append("<p><strong>Time:</strong> ").append(interview.getScheduledTime()).append("</p>");
        
        if ("VOICE_AI".equals(interviewType)) {
            email.append("<p><strong>Interview Type:</strong> <span style='color: #9c27b0;'>🤖 AI-Powered Voice Interview</span></p>");
            email.append("<p><strong>Mode:</strong> Online (Voice Call)</p>");
            
            // AI Interview Rounds
            email.append("<div class='rounds'>");
            email.append("<h4>🎯 Interview Rounds (3 Rounds)</h4>");
            email.append("<p><strong>Round 1: Technical Assessment</strong><br>AI will evaluate your technical skills and problem-solving abilities.</p>");
            email.append("<p><strong>Round 2: Behavioral Analysis</strong><br>AI will assess communication, teamwork, and cultural fit.</p>");
            email.append("<p><strong>Round 3: HR Evaluation</strong><br>AI will discuss expectations, availability, and final alignment.</p>");
            email.append("</div>");
            
            email.append("<p style='color: #666;'><em>Note: The AI interview is fully automated and conducted via voice call. Please ensure you're in a quiet environment with good internet connectivity.</em></p>");
            
        } else {
            email.append("<p><strong>Interview Type:</strong> Manual Interview with HR Team</p>");
            email.append("<p><strong>Mode:</strong> ").append(interview.getInterviewMode()).append("</p>");
            
            if ("ONLINE".equals(interview.getInterviewMode()) && interview.getMeetingLink() != null) {
                email.append("<p><strong>Meeting Link:</strong> <a href='").append(interview.getMeetingLink()).append("'>Join Meeting</a></p>");
            } else if ("OFFLINE".equals(interview.getInterviewMode()) && interview.getVenue() != null) {
                email.append("<p><strong>Venue:</strong> ").append(interview.getVenue()).append("</p>");
            }
            
            if (interview.getInterviewerNames() != null && !interview.getInterviewerNames().isEmpty()) {
                email.append("<p><strong>Interviewers:</strong> ").append(String.join(", ", interview.getInterviewerNames())).append("</p>");
            }
        }
        
        if (interview.getNotes() != null && !interview.getNotes().isEmpty()) {
            email.append("<p><strong>Additional Instructions:</strong><br>").append(interview.getNotes()).append("</p>");
        }
        
        email.append("</div>");
        
        // Action Button
        String interviewLink = frontendUrl + "/interview/" + interview.getId();
        email.append("<div style='text-align: center;'>");
        email.append("<a href='").append(interviewLink).append("' class='button'>");
        
        if ("VOICE_AI".equals(interviewType)) {
            email.append("🚀 Start AI Interview");
        } else {
            email.append("📅 View Interview Details");
        }
        email.append("</a>");
        email.append("</div>");
        
        // Preparation Tips
        email.append("<div class='info-box'>");
        email.append("<h3>💡 Preparation Tips</h3>");
        
        if ("VOICE_AI".equals(interviewType)) {
            email.append("<ul>");
            email.append("<li>Find a quiet space with minimal background noise</li>");
            email.append("<li>Ensure stable internet connection</li>");
            email.append("<li>Have your resume and relevant documents ready</li>");
            email.append("<li>Speak clearly and confidently</li>");
            email.append("<li>Listen carefully to each question before answering</li>");
            email.append("</ul>");
        } else {
            email.append("<ul>");
            email.append("<li>Review the job description thoroughly</li>");
            email.append("<li>Research about our company</li>");
            email.append("<li>Prepare examples of your work and achievements</li>");
            email.append("<li>Test your internet connection and meeting link</li>");
            email.append("<li>Dress professionally and arrive/login 5 minutes early</li>");
            email.append("</ul>");
        }
        email.append("</div>");
        
        email.append("<p>We look forward to speaking with you!</p>");
        email.append("<p>Best regards,<br><strong>").append(companyName).append(" HR Team</strong></p>");
        
        email.append("<div class='footer'>");
        email.append("<p>This is an automated email. Please do not reply to this email.</p>");
        email.append("<p>If you have any questions, please contact us at <a href='mailto:").append(companyEmail).append("'>")
            .append(companyEmail).append("</a></p>");
        email.append("<p>&copy; 2024 ").append(companyName).append(". All rights reserved.</p>");
        email.append("</div>");
        
        email.append("</div>");
        email.append("</body>");
        email.append("</html>");
        
        return email.toString();
    }
    
    /**
     * Send interview reminder (can be scheduled for 24 hours before)
     */
    public boolean sendInterviewReminder(Interview interview) {
        try {
            log.info("📧 Sending interview reminder to: {}", interview.getCandidateEmail());
            
            String subject = String.format("🔔 Reminder: Interview Tomorrow - %s", interview.getJobTitle());
            String body = buildReminderEmail(interview);
            
            // Log email
            log.info("Reminder email sent to: {}", interview.getCandidateEmail());
            
            return true;
            
        } catch (Exception e) {
            log.error("Error sending reminder: ", e);
            return false;
        }
    }
    
    /**
     * Build reminder email
     */
    private String buildReminderEmail(Interview interview) {
        return String.format(
            "Dear %s,\n\n" +
            "This is a friendly reminder about your interview scheduled for tomorrow.\n\n" +
            "Position: %s\n" +
            "Date: %s\n" +
            "Time: %s\n\n" +
            "Please make sure you're prepared and ready for the interview.\n\n" +
            "Best regards,\n" +
            "%s HR Team",
            interview.getCandidateName(),
            interview.getJobTitle(),
            interview.getScheduledDate().format(DATE_FORMATTER),
            interview.getScheduledTime(),
            companyName
        );
    }
}
