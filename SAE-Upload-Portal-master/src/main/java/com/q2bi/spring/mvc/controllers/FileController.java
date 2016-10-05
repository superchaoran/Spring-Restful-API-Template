package com.q2bi.spring.mvc.controllers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.exceptions.InvalidPdfException;

import com.q2bi.spring.mvc.model.FileMeta;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Controller
@RequestMapping("/controller")
public class FileController {

	
	protected LinkedList<FileMeta> files = new LinkedList<FileMeta>();
	
	/***************************************************
	 * URL: /rest/controller/upload  
	 * upload(): receives files
	 * @param request : MultipartHttpServletRequest auto passed
	 * @param response : HttpServletResponse auto passed
	 * @return LinkedList<FileMeta> as json format
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 ****************************************************/
	@RequestMapping(value="/upload", method = RequestMethod.POST)
	public @ResponseBody LinkedList<FileMeta> upload(MultipartHttpServletRequest request, HttpServletResponse response) throws ClassNotFoundException, SQLException {
 
		//1. build an iterator
		 Iterator<String> itr =  request.getFileNames();
		 MultipartFile mpf = null;
		 //2. get each file
		 while(itr.hasNext()){
			 
			 synchronized(this){
				 //2.1 get next MultipartFile
	             mpf = request.getFile(itr.next()); 
	             System.out.println(mpf.getOriginalFilename() +" uploaded! "+files.size());
	             //2.3 create new fileMeta
	             FileMeta fileMeta = new FileMeta();
	             fileMeta.setFileName(mpf.getOriginalFilename());
	             fileMeta.setFileSize(mpf.getSize()/1024+" Kb");
	             fileMeta.setFileType(mpf.getContentType());
				 
				 try {
					fileMeta.setBytes(mpf.getBytes());
					Boolean isPDF=extractionFromPDFAndInsertionToDatabase(mpf.getBytes());
					if(isPDF){
						fileMeta.setFileStatus("Success! Now in database.");
						fileMeta.setFileLink("<a href='rest/controller/get/"+files.size()+"'>Click</a>");
						// copy file to local disk (make sure the path "e.g. D:/temp/files" exists)
						FileCopyUtils.copy(mpf.getBytes(), new FileOutputStream("/Users/chaoran/temp/"+mpf.getOriginalFilename()));
						fileMeta.setFileDate(new Date());
					}else{
						fileMeta.setFileStatus("Rejected! Not a PDF.");
						fileMeta.setFileLink("N.A.");
						fileMeta.setFileDate(null);
					}
		            //FileSystemUtils.deleteRecursively(File("/Users/chaoran/temp/"+mpf.getOriginalFilename()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 
				 //2.4 add to files
				 files.add(fileMeta);
			 }
			 
		 }
		 
		// result will be like this
		// [{"fileName":"app_engine-85x77.png","fileSize":"8 Kb","fileType":"image/png"},...]
		return files;
 
	}
	
	public boolean extractionFromPDFAndInsertionToDatabase(byte[] pdfIn) throws IOException, SQLException, ClassNotFoundException{
		PdfReader reader;
		try{
			reader= new PdfReader(pdfIn);
		}catch(InvalidPdfException e){
			return false;
		}
    	AcroFields fields = reader.getAcroFields();
  	  	
        // This will load the MySQL driver, each DB has its own driver
        Class.forName("com.mysql.jdbc.Driver");
        // Setup the connection with the DB
        Connection connect = DriverManager.getConnection("jdbc:mysql://localhost/SAE_Report_Form?"
        		+ "user=root&password=8515111q");

        PreparedStatement preparedStatement = connect
                .prepareStatement("insert into SAE_Report_Form.section0 values (default, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        preparedStatement.setString(1, fields.getField("Section0_AssignedCase#").toString());
        preparedStatement.setString(2, fields.getField("Section0_PrimaryAdverseEventTerm").toString());
        preparedStatement.setString(3, fields.getField("Section0_Page#").toString());
        preparedStatement.setString(4, fields.getField("Section0_PageOf#").toString());
        preparedStatement.setString(5, fields.getField("Section0_DateOfReport_DD").toString());
        preparedStatement.setString(6, fields.getField("Section0_DateOfReport_MMM").toString());
        preparedStatement.setString(7, fields.getField("Section0_DateOfReport_YYYY").toString());
        preparedStatement.setString(8, fields.getField("Section0_Protocol#").toString());
        preparedStatement.setString(9, fields.getField("Section0_Site#").toString());
        preparedStatement.setString(10, fields.getField("Section0_Initial_Report_Boolean").toString());
        preparedStatement.setString(11, fields.getField("Section0_FollowUpReportNum").toString());
        preparedStatement.setString(12, fields.getField("Section0_ProtocolTitle").toString());
        preparedStatement.executeUpdate();
        
  	    preparedStatement = connect
                .prepareStatement("insert into SAE_Report_Form.section1 values (default, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        preparedStatement.setString(1, fields.getField("Section1_Patient#").toString());
        preparedStatement.setString(2, fields.getField("Section0_AssignedCase#").toString());
        preparedStatement.setString(3, fields.getField("Section1_PatientInitials").toString());
        preparedStatement.setString(4, fields.getField("Section1_DateOfBirth_DD").toString());
        preparedStatement.setString(5, fields.getField("Section1_DateOfBirth_MMM").toString());
        preparedStatement.setString(6, fields.getField("Section1_DateOfBirth_YYYY").toString());
        preparedStatement.setString(7, fields.getField("Section1_Sex_Radio").toString());
        preparedStatement.setString(8, fields.getField("Section1_Weight").toString());
        preparedStatement.setString(9, fields.getField("Section1_Weight_Unit_Radio").toString());
        preparedStatement.setString(10, fields.getField("Section1_Height").toString());
        preparedStatement.setString(11, fields.getField("Section1_Height_Radio").toString());
        preparedStatement.setString(12, fields.getField("Section1_Race_Radio").toString());
        preparedStatement.executeUpdate();
        return true;
	}
	
		
	@RequestMapping(value="/currentFiles", method = {RequestMethod.GET, RequestMethod.POST})
	public @ResponseBody LinkedList<FileMeta> homePage(HttpServletResponse response){
		return files;
	}
	
	
	/***************************************************
	 * URL: /rest/controller/get/{value}
	 * get(): get file as an attachment
	 * @param response : passed by the server
	 * @param value : value from the URL
	 * @return void
	 ****************************************************/
	@RequestMapping(value = "/get/{value}", method = RequestMethod.GET)
	 public void get(HttpServletResponse response,@PathVariable String value){
		 FileMeta getFile = files.get(Integer.parseInt(value));
		 try {		
			 	response.setContentType(getFile.getFileType());
			 	response.setHeader("Content-disposition", "attachment; filename=\""+getFile.getFileName()+"\"");
		        FileCopyUtils.copy(getFile.getBytes(), response.getOutputStream());
		 }catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		 }
	 }
 
}
