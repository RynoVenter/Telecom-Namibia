package na.telecom.telecompos;

import java.util.*;
import java.text.*;
import java.lang.Character.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;


public class SetSessionHeaderInfo extends VtiUserExit
{//The name no longer describe it's function, it's doing more misc functionality now.
	
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
		if (sessionHeader == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");

		VtiUserExitScreenField userIDField = getScreenField("USERID");
		VtiUserExitScreenField versionIDField = getScreenField("CLASSVERSION");
		VtiUserExitScreenField passwIDField = getScreenField("PASSWORD");
		VtiUserExitScreenField newPasswIDField = getScreenField("NEW_PASSWORD");
		VtiUserExitScreenField conFPasswIDField = getScreenField("CONF_PASSWORD");
		
		if (userIDField == null) return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
		
		if (passwIDField == null) return new VtiUserExitResult(999, "Screen field does not exist");
		if (!sessionHeader.getFunctionId().equalsIgnoreCase("YSPS_LOGIN"))
		{
			if (newPasswIDField == null) return new VtiUserExitResult(999, "Screen field does not exist");
			if (conFPasswIDField == null) return new VtiUserExitResult(999, "Screen field does not exist");
		}
		
		String validate = "";
		String usable = "";
		String sDate = "";
		
		Date currNow = new Date();
		String currLdbDate = DateFormatter.format("yyyyMMdd", currNow);
		
		boolean runValidate = false;

		sessionHeader.setUserId(userIDField.getFieldValue());
	//Initialise POSPole	
			VtiExitKeyValuePair[] keyOpen = 
			{
			};
	
		try
		{
			invokePrintTemplate("PoleInit", keyOpen);
		}
		catch (VtiExitException ee)
		{
		}
	
if (versionIDField != null)//Temporary, to be removed,will get the new version as soon as upgrade 1.07 has been done.
{
		//Validate and update password details.
		//Logon Password dataset
		VtiExitLdbTable logonlLdbTable = getLocalDatabaseTable("YSPS_LOGON");
		if (logonlLdbTable == null) return new VtiUserExitResult(999, "Unable to initialise table YSPS_LOGON.");
		
		VtiExitLdbSelectCriterion [] logonSelConds = 
				{
						new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, userIDField.getFieldValue()),
				};
        
		VtiExitLdbSelectConditionGroup logonSelCondGrp = new VtiExitLdbSelectConditionGroup(logonSelConds, true);
		VtiExitLdbTableRow[] logonLdbRows = logonlLdbTable.getMatchingRows(logonSelCondGrp);

		if(logonLdbRows.length == 0) return new VtiUserExitResult(999, "Unable to query table YSPS_LOGON.");
		
		usable = logonLdbRows[0].getFieldValue("USEABLE");
		validate = logonLdbRows[0].getStringFieldValue("PASSWORD");
		sDate = logonLdbRows[0].getFieldValue("RESETDATE");
		Log.trace(0,"sDate from table record = " + sDate);
		Log.trace(0,"sDate from table record length = " + sDate.length());
		
		Calendar dbCal = Calendar.getInstance();
		
		if(!passwIDField.getStringFieldValue().equals(validate))
			return new VtiUserExitResult(999,1,"Please ensure the correct username and password is used.");
		   
		if(sDate.length() > 7)
		{
			dbCal.clear();
			int y = Integer.parseInt(sDate.substring(0,4));
			int m = Integer.parseInt(sDate.substring(4,6));
			m--;
			int d = Integer.parseInt(sDate.substring(6,8));
			dbCal.set(y,m,d);
			Log.trace(0,"Date deconstruct for DBCal calendar is " + y + " /" + m + " /" + y);
			dbCal.add(Calendar.DATE,30);
		}
		
		Log.trace(0,"dbDate after 30 add is = " + dbCal.toString());
		
		Calendar cCal = Calendar.getInstance();
		
		
		Log.trace(0,"If results ");
		Log.trace(0,"If results " + !usable.equalsIgnoreCase("X") + " && " + !sessionHeader.getFunctionId().equalsIgnoreCase("YSPS_NEWLOGIN"));
		Log.trace(0,"If results " + sDate.length() + " && " + !sessionHeader.getFunctionId().equalsIgnoreCase("YSPS_NEWLOGIN"));
		Log.trace(0,"If results " + cCal.after(dbCal) + " && " + !sessionHeader.getFunctionId().equalsIgnoreCase("YSPS_NEWLOGIN"));
		Log.trace(0,"cCal calendar " + cCal.toString());
		
		if((!usable.equalsIgnoreCase("X") && !sessionHeader.getFunctionId().equalsIgnoreCase("YSPS_NEWLOGIN")) 
		   || (sDate.length() != 8 && !sessionHeader.getFunctionId().equalsIgnoreCase("YSPS_NEWLOGIN"))
		   || (cCal.after(dbCal) && !sessionHeader.getFunctionId().equalsIgnoreCase("YSPS_NEWLOGIN")))
			 sessionHeader.setNextFunctionId("YSPS_NEWLOGIN");
		else if(passwIDField.getFieldValue().equals(validate) && sessionHeader.getFunctionId().equalsIgnoreCase("YSPS_LOGIN"))
			sessionHeader.setNextFunctionId("YSPS_ORDER");
		else if(sessionHeader.getFunctionId().equalsIgnoreCase("YSPS_NEWLOGIN"))
		{
			if(passwIDField.getFieldValue().equals(validate)  && newPasswIDField.getFieldValue().equals(conFPasswIDField.getFieldValue()))
				sessionHeader.setNextFunctionId("YSPS_ORDER");
		}
		//Validate old Password and then validate new password, then set and save the new row to the table.
		if(!sessionHeader.getFunctionId().equalsIgnoreCase("YSPS_LOGIN"))
		{
			Log.trace(0, "new password");
			
			Log.trace(0, "Validation was as : Is current password valid " + passwIDField.getFieldValue().equals(validate) + " : Is the useable not marked " + !logonLdbRows[0].getFieldValue("USEABLE").equalsIgnoreCase("X") + " The data calc, was cCal after dbCal " + cCal.after(dbCal) + " was the date length 0 " + sDate.length());
			
			if((passwIDField.getFieldValue().equals(validate) && !logonLdbRows[0].getFieldValue("USEABLE").equalsIgnoreCase("X"))
				||(passwIDField.getFieldValue().equals(validate) && cCal.after(dbCal) 
				|| passwIDField.getFieldValue().equals(validate) && sDate.length() == 1))
			{
				
				Log.trace(0, "One of the checks was true");
				
				Log.trace(0, "What is the 2 values in the password checks: new pass  " + newPasswIDField.getFieldValue() + " con pass " + conFPasswIDField.getFieldValue() );
				Log.trace(0, "Are they equal " + newPasswIDField.getFieldValue().equals(conFPasswIDField.getFieldValue()));
				if(newPasswIDField.getFieldValue().equals(conFPasswIDField.getFieldValue()))
				{
					Log.trace(0, "New passwords are the same");
					if(newPasswIDField.getFieldValue().equals(validate))
					{
						newPasswIDField.setFieldValue("");
						conFPasswIDField.setFieldValue("");
						return new VtiUserExitResult(999, "Please provide a password not similar to previous passwords.");
					}
					if(newPasswIDField.getFieldValue().length() < 6)
					{
						newPasswIDField.setFieldValue("");
						conFPasswIDField.setFieldValue("");
						return new VtiUserExitResult(999, "The password needs to be at least 6 characters long.");
					}
					
					String sComplexPass = complexPassword(conFPasswIDField.getFieldValue());
					Log.trace(0, "Value from complex method is " + sComplexPass);
					if(!sComplexPass.equalsIgnoreCase("Complex"))
					{
						newPasswIDField.setFieldValue("");
						conFPasswIDField.setFieldValue("");
						return new VtiUserExitResult(999,1, sComplexPass);
					}
						
					for(int u = 0;u < logonLdbRows.length;u++)
					{
						Log.trace(0,"Saving new passwords to all user instances");
						logonLdbRows[u].setFieldValue("PASSWORD",conFPasswIDField.getFieldValue());
						logonLdbRows[u].setFieldValue("USEABLE","X");
						logonLdbRows[u].setFieldValue("RESETDATE",currLdbDate);
						logonLdbRows[u].setFieldValue("TIMESTAMP","");
					}
					
					try
					{					
					
					for(int s = 0;s < logonLdbRows.length;s++)
					{
						logonlLdbTable.saveRow(logonLdbRows[s]);
					}
							
						// Trigger the uploads to SAP, if a connection is available.
							String hostName = getHostInterfaceName();
							boolean hostConnected = isHostInterfaceConnected(hostName);

							if (hostConnected)
							{ 
								VtiExitLdbRequest ldbReqUploadNewPassword = new VtiExitLdbRequest(logonlLdbTable,VtiExitLdbRequest.UPLOAD);
								ldbReqUploadNewPassword.submit(false);
							}
					}
					catch (VtiExitException ee)
					{
						return new VtiUserExitResult(999, "Failed to update new password.");
					}
				}
			}
			else
			{
				//newPasswIDField.setHiddenFlag(true);
				//conFPasswIDField.setHiddenFlag(true);
				
				return new VtiUserExitResult(999, 1,"Please ensure that the current password is correct, also ensure that the New and Confirmation password is typed exactly the same.");
			}
			
				if(!newPasswIDField.getFieldValue().equals(conFPasswIDField.getFieldValue()))
				{
					newPasswIDField.setFieldValue("");
					conFPasswIDField.setFieldValue("");
					return new VtiUserExitResult(999, "Please ensure that the new password and confirmation password is the same.");
				}

			}
		}
		return new VtiUserExitResult();
	}
	
	private String complexPassword(String sPassword)
	{
		StringBuffer sResult = new StringBuffer();
		
		int iNumCount = 0;
		int iCapitalCount = 0;
		int iSpecialCount = 0;
				
		for(int i = 0;i < sPassword.length();i++)
		{
			String sChar = sPassword.substring(i, i+1);
			
			try
			{
				Integer.valueOf(sChar);
				iNumCount++;
			}
			catch (NumberFormatException nfe)
			{
			}
						
			if(Character.isUpperCase(sChar.charAt(0)))
			{
				iCapitalCount++;
			}
			
			if(sChar.equals("!")
			   || sChar.equals("@")
			   || sChar.equals("#")
			   || sChar.equals("$")
			   || sChar.equals("%")
			   || sChar.equals("^")
			   || sChar.equals("&")
			   || sChar.equals("*")
			   || sChar.equals("_")
			   || sChar.equals("-")
			   || sChar.equals("+")
			   || sChar.equals("=")
			   || sChar.equals(":")
			   || sChar.equals(";")
			   || sChar.equals("/")
			   || sChar.equals("?")
			   || sChar.equals(".")
			   || sChar.equals(",")
			   || sChar.equals("<")
			   || sChar.equals(">")
			   || sChar.equals("|")
			   || sChar.equals("~")
			   || sChar.equals("`")
			   || sChar.equals("(")
			   || sChar.equals(")"))
			{
				iSpecialCount++;

			}
		}
			
			if(iNumCount > 0 && iCapitalCount > 0 && iSpecialCount > 0)
				sResult.append("Complex");
			else if(iNumCount == 0)
				sResult.append("Please ensure password has atleast 1 number.");
			else if(iCapitalCount == 0)
				sResult.append("Please ensure password has atleast 1 capital letter.");
			else if(iSpecialCount == 0)
				sResult.append("Please ensure that the password has atleast one of the following characters. ( ~`!@#$%^&*_-+=:;|/?.,<>( ) )");
		

			
		return sResult.toString();
	}
}

