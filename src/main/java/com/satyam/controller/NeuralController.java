package com.satyam.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.satyam.entity.DocumentChunk;
import com.satyam.entity.MyUser;
import com.satyam.jwt.JwtUtil;
import com.satyam.mail.MailSending;
import com.satyam.repo.UserRepo;
import com.satyam.service.AiService;
import com.satyam.service.DocumentService;
import com.satyam.service.UserService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class NeuralController {

   
	
	@Autowired
	private UserService userservice;
	@Autowired
	private DocumentService docService;
	@Autowired
	private AiService aiService;
	@Autowired
	private MailSending mailSending;
	@Autowired
	private JwtUtil jwtutil;
	
	

    

	@RequestMapping("/")
	public String home() {
		return "index";
	}
	
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	@GetMapping("/dashboard")
	public String userHome(Model model, Principal principal) {
		MyUser user= userservice.getUserByUsername(principal.getName());
		model.addAttribute("username", user.getName());
		return "dashboard";
	}
	@GetMapping("/upload_pdf")
	public String upload() {
		return "upload_pdf";
	}
	@GetMapping("/ask_question")
	public String askQuestion() {
		return "ask_question";
	}
	@GetMapping("/forget_password")
	public String forgetPassword() {
		return "forget_password";
	}
	@GetMapping("/reset_password")
	public String resetPassword(@RequestParam String token,Model m) {
		
		String email=jwtutil.validateToken(token);
		
		if(email==null) { 
			m.addAttribute("msg", "Invalid or Expired token!");
			return "login";
		}
		
		return "reset_password";
	}
	
	@GetMapping("/my_document")
	public String myDocument(Model m, Principal principal) {
		
		String userName=principal.getName();
		
		List<DocumentChunk>docs=docService.myDocument(userName);
		
		m.addAttribute("documents", docs);
		return "my_document";
	}
	@GetMapping("/my_profile")
	public String getMethodName(Model m, Principal principal) {
		
		MyUser user=userservice.getUserByUsername(principal.getName());
		
		m.addAttribute("user", user);
		
		return "my_profile";
	}
	
	@GetMapping("/getProfileImage")
	public void getProfileImage(Principal principal, HttpServletResponse response) throws IOException {

	    MyUser user = userservice.getUserByUsername(principal.getName());
	    byte[] image;
	    if (user == null || user.getPhoto() == null || user.getPhoto().length == 0) {

	        InputStream is = getClass()
	                .getClassLoader()
	                .getResourceAsStream("static/images/defaultImage.png");

	        if (is == null) {
	            response.sendError(HttpServletResponse.SC_NOT_FOUND);
	            return;
	        }

	        image = is.readAllBytes();
	        response.setContentType("image/png");

	    } else {
	        image = user.getPhoto();
	        response.setContentType("image/jpeg");
	    }

	    
	    response.getOutputStream().write(image);
	    response.getOutputStream().flush();
	}
	
	@GetMapping("/download")
	public String downloadPDF(@RequestParam Long id,HttpServletResponse response) throws IOException {
		DocumentChunk doc=docService.getDocument(id);
		if(doc!=null) {
			byte []docs=doc.getDocument();
			response.setHeader("Content-Disposition", "attachment;filename="+ doc.getName()+".pdf");
			response.getOutputStream().write(docs);
		}
		return "my_document";
	}
	@GetMapping("/delete")
	public String deletePDF(@RequestParam Long id,Model m, Principal principal) throws IOException {
		
		DocumentChunk obj=docService.getDocument(id);
		
		if(obj!=null) {
			docService.deleteDoc(obj.getId());
		}
		
		String userName=principal.getName();
		
		List<DocumentChunk>docs=docService.myDocument(userName);
		
		m.addAttribute("documents", docs);
		
		return "my_document";
	}
	@GetMapping("/changePassword")
	public String changePasswordd() {
		return "changepassword";
	}

	@PostMapping("/signup")
	public String register( @ModelAttribute MyUser myUser, @RequestPart MultipartFile img, Model m) throws IOException {
		byte[] photo=img.getBytes();
		if(photo.length!=0) {
			myUser.setPhoto(photo);
		}else {
			myUser.setPhoto(null);
		}
		if(userservice.save(myUser)) {
			m.addAttribute("msg", "User add SuccessFully!");
			
		}else {
			m.addAttribute("msg", "User already Exist!");
		}
		
		return "login";
	}
	
	@GetMapping("/ask")
	public String getDocument(Principal principal, Model m) {
		
		String userName=principal.getName();
		List<DocumentChunk> docs = docService.myDocument(userName);
		m.addAttribute("documents", docs);
		return "ask_question";
	}
	
	
	
	@PostMapping("/ask")
	public String ask(@RequestParam Long documentId,@RequestParam String question,Principal principal,Model model) {

	    String username = principal.getName();

	   
	    DocumentChunk doc = docService.getDocumentByIdAndUser(documentId, username);

	    
	    String answer = aiService.askFromDocument(question, doc.getContent());

	   
	    model.addAttribute("question", question);
	    model.addAttribute("answer", answer);

	   
	    model.addAttribute("documents",
	            docService.myDocument(username));

	    return "ask_question";
	}

	@PostMapping("/upload")
	public String uploadPdf(@RequestPart MultipartFile file,Principal principle, Model m) {
		MyUser user=userservice.getUserByUsername(principle.getName());
		
		String result= docService.save(file , user);
		
		if(result.equalsIgnoreCase("success")) {
			m.addAttribute("msg", "File Uploaded SuccessFully!");
		}else if(result.equalsIgnoreCase("Already")) {
			m.addAttribute("msg", "File Already Exist!");
		}else if(result.equalsIgnoreCase("SizeExceed")) {
			m.addAttribute("msg", "File Size Exceed!");
		}else {
			m.addAttribute("msg", "File Upload Failed!");
		}
		
		return "upload_pdf";
	}
	@PostMapping("/forget")
	public String forget(@RequestParam String email, Model m) {
		
		MyUser user=userservice.getUserByUsername(email);
		
		if(user==null) {
			m.addAttribute("msg", "User Does Not Exist!");
		}else {
//			String s=mailSending.mailSendResetPassword(email);
			String s=mailSending.mailSendResetPasswordHTML(email);
			if(s.equalsIgnoreCase("success")) {
				m.addAttribute("msg", "Password Reset Link Mail Send!");
			}else {
				m.addAttribute("msg", " Mail Send Failed!");
			}
		}
		
		return "login";
	}
	@PostMapping("/update_password")
	public String updatePassword(@RequestParam String newPassword,@RequestParam String confirmPassword, @RequestParam String token,Model m ) {
		
		String email=jwtutil.validateToken(token);
		
		if(email==null) {
			m.addAttribute("msg", "Invalid or Expired Token!");
		}else {
			if(newPassword.equals(confirmPassword)) {
				if(userservice.updatePassword(newPassword, email)) {
					m.addAttribute("msg", "Password Updated!");
				}else {
					m.addAttribute("msg", "User Not Found!");
				}
			}else {
				m.addAttribute("msg", "Password Mismatched!");
				return "reset_password";
			}
		}
		
		return "login";
	}
	
	@PostMapping("/uploadProfileImage")
	public String uploadProfileImage(Principal principal , @RequestPart MultipartFile img,Model m) throws IOException {
		MyUser user=userservice.getUserByUsername(principal.getName());
		
		String s=userservice.updateProfileImage(user, img);
		if(s.equalsIgnoreCase("success")) {
			m.addAttribute("msg", "Profile Updated Successfully!");
		}else {
			m.addAttribute("msg", "User Not Found!");
		}
		MyUser u=userservice.getUserByUsername(principal.getName());
		
		m.addAttribute("user", u);
		
		return "my_profile";
	}
	@PostMapping("/change_password")
	public String changePassword(Principal principal, @RequestParam String oldPassword,@RequestParam String newPassword, @RequestParam String  confirmPassword,Model m) {
		String userName=principal.getName();
		if( !oldPassword.equals(newPassword)&&newPassword.equals(confirmPassword)) {
			boolean result=userservice.changePassword(userName,oldPassword,newPassword);
			if(result) {
				m.addAttribute("success", "Password Changed Successfully!");
				
			}else {
				m.addAttribute("error", "Enter Correct Password!");
			}
			
		}else if(oldPassword.equals(newPassword) || oldPassword.equals(confirmPassword)){
			m.addAttribute("error", "Enter a password that has not been used before!");
			
			
		}else {
			m.addAttribute("error", "Password Mismatched!");
		}
			
		return "changepassword";
	}
	
	@GetMapping("/document/view/{id}")
	public void viewPDF(@PathVariable Long id, HttpServletResponse response) throws IOException {

	    DocumentChunk doc = docService.getDocument(id);

	    if (doc == null || doc.getDocument() == null) {
	        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	        return;
	    }

	    byte[] docs = doc.getDocument();

	    response.setContentType("application/pdf");
	    response.setContentLength(docs.length);
//	    response.setHeader(
//	        "Content-Disposition",
//	        "inline; filename=\"" + doc.getName() + ".pdf\""
//	    );

	    OutputStream out = response.getOutputStream();
	    out.write(docs);
//	    out.flush();
	    out.close();
	}
	
	
	
	
	
}
