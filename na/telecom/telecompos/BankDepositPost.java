package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;
import au.com.skytechnologies.ecssdk.util.*;

import java.util.*;
import java.text.*;

public class BankDepositPost extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Data Declarations.     
		VtiUserExitScreenField TodayDate = getScreenField("DATE");	  
		VtiUserExitScreenField UserId = getScreenField("USERID");
		VtiUserExitScreenField ServerId = getScreenField("SERVERID");
		VtiUserExitScreenField Cheque = getScreenField("CHEQUE");
		VtiUserExitScreenField Cash = getScreenField("CASH"); 
		VtiUserExitScreenField N10 = getScreenField("N10_QTY");
		VtiUserExitScreenField N20 = getScreenField("N20_QTY");
		VtiUserExitScreenField N50 = getScreenField("N50_QTY");
		VtiUserExitScreenField N100 = getScreenField("N100_QTY");
		VtiUserExitScreenField N200 = getScreenField("N200_QTY");
		VtiUserExitScreenField CoinValue = getScreenField("COIN_VALUE");
		VtiUserExitScreenField ZAR10 = getScreenField("ZAR10_QTY");
		VtiUserExitScreenField ZAR20 = getScreenField("ZAR20_QTY");
		VtiUserExitScreenField ZAR50 = getScreenField("ZAR50_QTY");
		VtiUserExitScreenField ZAR100 = getScreenField("ZAR100_QTY");
		VtiUserExitScreenField ZAR200 = getScreenField("ZAR200_QTY");
		VtiUserExitScreenField CoinValueZAR = getScreenField("COINR_VALUE");
		VtiUserExitScreenField TakeOnFloat = getScreenField("FLOAT");
		VtiUserExitScreenField TotalCashAfterFloat = getScreenField("TOTAL_DUE");
		VtiUserExitScreenField TotalEntered = getScreenField("DEPOSIT_VALUE");
		VtiUserExitScreenField CashDifference = getScreenField("DIFFERENCE");
		VtiUserExitScreenField ChequeCount = getScreenField("DIFFERENCE_CQ");
		VtiUserExitScreenField ChequeDue = getScreenField("DUE");
		VtiUserExitScreenField dueChd = getScreenField("SUPER");	
		
		//Screen table declarations
		VtiUserExitScreenTable ChequeScrTable = getScreenTable("CHEQUE");

		//LDB table declaration
		VtiExitLdbTable YSPS_TILL_CASH = getLocalDatabaseTable("YSPS_TILL_CASH");
		VtiExitLdbTable YSPS_TILL_CHEQUE = getLocalDatabaseTable("YSPS_TILL_CHEQUE");
		VtiExitLdbTable YSPS_PAYMENT_TRANSACTION = getLocalDatabaseTable("YSPS_PAYMENT_TRANSACTION");
		

		if (TodayDate == null) return new VtiUserExitResult(999, "Screen Field DATE does not exist");
		if (UserId == null) return new VtiUserExitResult(999, "Screen Field USERID does not exist");    
		if (ServerId== null) return new VtiUserExitResult(999, "Screen Field SERVERID does not exist");
		if (Cheque== null) return new VtiUserExitResult(999, "Screen Field CHEQUE does not exist");
		if (Cash== null) return new VtiUserExitResult(999, "Screen Field CASH does not exist");
		if (N10== null) return new VtiUserExitResult(999, "Screen Field N10_QTY does not exist");
		if (N20== null) return new VtiUserExitResult(999, "Screen Field N20_QTY does not exist");
		if (N50== null) return new VtiUserExitResult(999, "Screen Field N50_QTY does not exist");
		if (N100== null) return new VtiUserExitResult(999, "Screen Field N100_QTY does not exist");
		if (N200== null) return new VtiUserExitResult(999, "Screen Field N200_QTY does not exist");
		if (CoinValue== null) return new VtiUserExitResult(999, "Screen Field COIN_VALUE does not exist");
		if (ZAR10== null) return new VtiUserExitResult(999, "Screen Field ZAR10_QTY does not exist");
		if (ZAR20== null) return new VtiUserExitResult(999, "Screen Field ZAR10_QTY does not exist");
		if (ZAR50== null) return new VtiUserExitResult(999, "Screen Field ZAR10_QTY does not exist");
		if (ZAR100== null) return new VtiUserExitResult(999, "Screen Field ZAR10_QTY does not exist");
		if (ZAR200== null) return new VtiUserExitResult(999, "Screen Field ZAR10_QTY does not exist");
		if (CoinValueZAR== null) return new VtiUserExitResult(999, "Screen Field COINR_VALUE does not exist");
		if (TakeOnFloat== null) return new VtiUserExitResult(999, "Screen Field FLOAT does not exist");
		if (TotalCashAfterFloat== null) return new VtiUserExitResult(999, "Screen Field TOTAL_DUE does not exist");
		if (TotalEntered== null) return new VtiUserExitResult(999, "Screen Field DEPOSIT_VALUE does not exist");
		if (CashDifference== null) return new VtiUserExitResult(999, "Screen Field DIFFERENCE does not exist");
		if (ChequeCount== null) return new VtiUserExitResult(999, "Screen Field DIFFERENCE_CQ does not exist");
		if (ChequeDue== null) return new VtiUserExitResult(999, "Screen Field DUE does not exist");
		if (dueChd== null) return new VtiUserExitResult(999, "Screen Field SUPER does not exist");
		
		if (ChequeScrTable== null) return new VtiUserExitResult(999, "Screen TABLE CHEQUE does not exist");
		if (YSPS_TILL_CASH== null) return new VtiUserExitResult(999, "LDB YSPS_TILL_CASH does not exist");
		if (YSPS_TILL_CHEQUE== null) return new VtiUserExitResult(999, "LDB YSPS_TILL_CHEQUE does not exist");
		if (YSPS_PAYMENT_TRANSACTION== null) return new VtiUserExitResult(999, "LDB YSPS_PAYMENT_TRANSACTION does not exist");

		
		String strDate = TodayDate.getFieldValue();
		String strServerId = ServerId.getFieldValue();
		String strServerGroup  = getServerGroup();
//		String strCash = Cash.getFieldValue();
		String strUserid = UserId.getFieldValue();
		double dblN10 = N10.getDoubleFieldValue();
		double dblN20 = N20.getDoubleFieldValue();
		double dblN50 = N50.getDoubleFieldValue();
		double dblN100 = N100.getDoubleFieldValue();
		double dblN200 = N200.getDoubleFieldValue();
		double dblNCoins = CoinValue.getDoubleFieldValue();
		double dblZ10 = ZAR10.getDoubleFieldValue();
		double dblZ20 = ZAR20.getDoubleFieldValue();
		double dblZ50 = ZAR50.getDoubleFieldValue();
		double dblZ100 = ZAR100.getDoubleFieldValue();
		double dblZ200 = ZAR200.getDoubleFieldValue();
		double dblZCoins = CoinValueZAR.getDoubleFieldValue();
		
		DecimalFormat df1 = new DecimalFormat("######0.00");
		
		double dblTotalCash= TotalEntered.getDoubleFieldValue();
		double dblTotalDue =  TotalCashAfterFloat.getDoubleFieldValue();
		double dblTakeOnFloat = TakeOnFloat.getDoubleFieldValue();
		double dblDifference = CashDifference.getDoubleFieldValue();
	 
		//See if the recon have been done already
		
		try
        {
			VtiExitLdbSelectCriterion[] tillRecSelConds =
				{
					new VtiExitLdbSelectCondition("SERVER_ID", VtiExitLdbSelectCondition.EQ_OPERATOR, getVtiServerId()),
					new VtiExitLdbSelectCondition("TILL_DATE", VtiExitLdbSelectCondition.EQ_OPERATOR, TodayDate.getFieldValue()),
					new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, UserId.getFieldValue())
				};
					
			VtiExitLdbSelectConditionGroup tillRecSelCondsGrp = new VtiExitLdbSelectConditionGroup(tillRecSelConds, true);
  
			VtiExitLdbTableRow [] tillRecLdbRows = YSPS_TILL_CASH.getMatchingRows(tillRecSelCondsGrp);
			
			if(tillRecLdbRows.length > 0)
				return new VtiUserExitResult(999, "The recon for this till has been done.");
            
        }
        catch (VtiExitException ee)
        {
            return new VtiUserExitResult(999, "Failed to query database.");
        }

		//Check if the right amount of cash was entered per denomination
		if ((N10.getDoubleFieldValue() % 10) > 0) return new VtiUserExitResult(999, "The cash amount for the N$ 10 denominations is not possible.");
		if ((N20.getDoubleFieldValue() % 20) > 0) return new VtiUserExitResult(999, "The cash amount for the N$ 20 denominations is not possible.");
		if ((N50.getDoubleFieldValue() % 50) > 0) return new VtiUserExitResult(999, "The cash amount for the N$ 50 denominations is not possible.");
		if ((N100.getDoubleFieldValue() % 100) > 0) return new VtiUserExitResult(999, "The cash amount for the N$ 100 denominations is not possible.");
		if ((N200.getDoubleFieldValue() % 200) > 0) return new VtiUserExitResult(999, "The cash amount for the N$ 200 denominations is not possible.");
		if ((ZAR10.getDoubleFieldValue() % 10) > 0) return new VtiUserExitResult(999, "The cash amount for the R 10 denominations is not possible.");
		if ((ZAR20.getDoubleFieldValue() % 20) > 0) return new VtiUserExitResult(999, "The cash amount for the R 20 denominations is not possible.");
		if ((ZAR50.getDoubleFieldValue() % 50) > 0) return new VtiUserExitResult(999, "The cash amount for the R 50 denominations is not possible.");
		if ((ZAR100.getDoubleFieldValue() % 100) > 0) return new VtiUserExitResult(999, "The cash amount for the R 100 denominations is not possible.");
		if ((ZAR200.getDoubleFieldValue() % 200) > 0) return new VtiUserExitResult(999, "The cash amount for the R 200 denominations is not possible.");

		//is diff and flag not X
		if(dblDifference!=0  && !dueChd.getFieldValue().equalsIgnoreCase("X"))
		{
			return new VtiUserExitResult(999,"Cannot post difference amount. Inform supervisor to override");
		}
		
		VtiExitLdbTableRow TillCashLDBRow = YSPS_TILL_CASH.newRow();
		
		TillCashLDBRow.setFieldValue("TILL_DATE", strDate);
		TillCashLDBRow.setFieldValue("SERVERGRP", strServerGroup);
		TillCashLDBRow.setFieldValue("SERVER_ID", strServerId);
		TillCashLDBRow.setFieldValue("USERID", strUserid);
		TillCashLDBRow.setFieldValue("CASH_FLOAT", dblTakeOnFloat);
		TillCashLDBRow.setFieldValue("TOTAL_DUE", dblTotalDue);
		TillCashLDBRow.setFieldValue("TOTAL_CAPTURED", dblTotalCash);
		TillCashLDBRow.setFieldValue("DIFFERENCE", dblDifference);
		TillCashLDBRow.setFieldValue("N10", dblN10);
		TillCashLDBRow.setFieldValue("N20", dblN20);
		TillCashLDBRow.setFieldValue("N50", dblN50);
		TillCashLDBRow.setFieldValue("N100", dblN100);
		TillCashLDBRow.setFieldValue("N200", dblN200);
		TillCashLDBRow.setFieldValue("NCOINS", dblNCoins);
		TillCashLDBRow.setFieldValue("ZAR10", dblZ10);
		TillCashLDBRow.setFieldValue("ZAR20", dblZ20);
		TillCashLDBRow.setFieldValue("ZAR50", dblZ50);
		TillCashLDBRow.setFieldValue("ZAR100", dblZ100);
		TillCashLDBRow.setFieldValue("ZAR200", dblZ200);
		TillCashLDBRow.setFieldValue("ZARCOINS", dblZCoins);
		TillCashLDBRow.setFieldValue("TIMESTAMP", "");
		TillCashLDBRow.setFieldValue("DEL_IND", "");
		
		YSPS_TILL_CASH.saveRow(TillCashLDBRow);
		
		for(int i=0; i<ChequeScrTable.getRowCount();i++)
		{
			VtiUserExitScreenTableRow chequeRow = ChequeScrTable.getRow(i);
			String strChequeNo = chequeRow.getFieldValue("CHQ_NO");
			String strOrderNo = chequeRow.getFieldValue("ORDER_NO");
			double dblAmount = chequeRow.getDoubleFieldValue("AMOUNT");
			
			VtiExitLdbTableRow TillChequeLDBRow = YSPS_TILL_CHEQUE.newRow();
			
			TillChequeLDBRow.setFieldValue("TILL_DATE",strDate);
			TillChequeLDBRow.setFieldValue("SERVERGRP", strServerGroup);
			TillChequeLDBRow.setFieldValue("SERVER_ID", strServerId);
			TillChequeLDBRow.setFieldValue("USERID", strUserid);
			TillChequeLDBRow.setFieldValue("ORDER_NO",strOrderNo);
			TillChequeLDBRow.setFieldValue("CHEQUE_NO",strChequeNo);
			TillChequeLDBRow.setFieldValue("AMOUNT",dblAmount);
			TillChequeLDBRow.setFieldValue("TIMESTAMP","");
			TillChequeLDBRow.setFieldValue("DEL_IND","");
			
			YSPS_TILL_CHEQUE.saveRow(TillChequeLDBRow);
			
		}
		printSlip();
		return new VtiUserExitResult(000,"Successfully posted");

	}
	//Class Method
	
	private VtiUserExitResult printSlip() throws VtiExitException
	{
		//Screen Field Declarations
		VtiUserExitScreenField currScrFFloat = getScreenField("FLOAT");
		VtiUserExitScreenField currScrFTDue = getScreenField("TOTAL_DUE");
		VtiUserExitScreenField currScrFCapt = getScreenField("DEPOSIT_VALUE");
		VtiUserExitScreenField currScrFDiff = getScreenField("DIFFERENCE");
		VtiUserExitScreenField currScrFTCheq = getScreenField("DIFFERENCE_CQ");
		VtiUserExitScreenField currScrFTCDue = getScreenField("DUE");
		VtiUserExitScreenField user = getScreenField("USERID");
		VtiUserExitScreenField date = getScreenField("DATE");
		VtiUserExitScreenField currScrFN10 = getScreenField("N10_QTY");
		VtiUserExitScreenField currScrFN20 = getScreenField("N20_QTY");
		VtiUserExitScreenField currScrFN50 = getScreenField("N50_QTY");
		VtiUserExitScreenField currScrFN100 = getScreenField("N100_QTY");
		VtiUserExitScreenField currScrFN200 = getScreenField("N200_QTY");
		VtiUserExitScreenField currScrFNCoin = getScreenField("COIN_VALUE");
		VtiUserExitScreenField currScrFR10 = getScreenField("ZAR10_QTY");
		VtiUserExitScreenField currScrFR20 = getScreenField("ZAR20_QTY");
		VtiUserExitScreenField currScrFR50 = getScreenField("ZAR50_QTY");
		VtiUserExitScreenField currScrFR100 = getScreenField("ZAR100_QTY");
		VtiUserExitScreenField currScrFR200 = getScreenField("ZAR200_QTY");
		VtiUserExitScreenField currScrFRCoin = getScreenField("COINR_VALUE");
		
		//Screen Table Declaration
		VtiUserExitScreenTable scrTCheq = getScreenTable("CHEQUE");

		
		//Screen Field Validation
		if (currScrFFloat == null) return new VtiUserExitResult(999, "Screen Field FLOAT does not exist");
		if (currScrFTDue == null) return new VtiUserExitResult(999, "Screen Field TOTAL_DUE does not exist");
		if (currScrFCapt == null) return new VtiUserExitResult(999, "Screen Field DEPOSIT_VAL does not exist");
		if (currScrFDiff == null) return new VtiUserExitResult(999, "Screen Field DIFFERENCE does not exist");
		if (currScrFTCheq == null) return new VtiUserExitResult(999, "Screen Field DIFFERENCE_CQ does not exist");
		if (currScrFTCDue == null) return new VtiUserExitResult(999, "Screen Field DUE does not exist");
		if (currScrFN10 == null) return new VtiUserExitResult(999, "Screen Field N10_QTY does not exist");
		if (currScrFN20 == null) return new VtiUserExitResult(999, "Screen Field N20_QTY does not exist");
		if (currScrFN50 == null) return new VtiUserExitResult(999, "Screen Field N50_QTY does not exist");
		if (currScrFN100 == null) return new VtiUserExitResult(999, "Screen Field N100_QTY does not exist");
		if (currScrFN200 == null) return new VtiUserExitResult(999, "Screen Field N200_QTY does not exist");
		if (currScrFNCoin == null) return new VtiUserExitResult(999, "Screen Field COIN_VALUE does not exist");
		if (currScrFR10 == null) return new VtiUserExitResult(999, "Screen Field ZAR10_QTY does not exist");
		if (currScrFR20 == null) return new VtiUserExitResult(999, "Screen Field ZAR20_QTY does not exist");
		if (currScrFR50 == null) return new VtiUserExitResult(999, "Screen Field ZAR50_QTY does not exist");
		if (currScrFR100 == null) return new VtiUserExitResult(999, "Screen Field ZAR100_QTY does not exist");
		if (currScrFR200 == null) return new VtiUserExitResult(999, "Screen Field ZAR200_QTY does not exist");
		if (currScrFRCoin == null) return new VtiUserExitResult(999, "Screen Field COINR_VALUE does not exist");
		if (user == null) return new VtiUserExitResult(999, "Screen Field USERID does not exist");
		if (date == null) return new VtiUserExitResult(999, "Screen Field DATE does not exist");
		
		if (scrTCheq == null) return new VtiUserExitResult(999, "Screen Table CHEQUE does not exist");
		//LDB Declaration
		VtiExitLdbTable ldbLogonLdbTable = getLocalDatabaseTable("YSPS_LOGON");
  
		if (ldbLogonLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_LOGON not found");
		
		//Attribute & Constant Declaration
		double totCheq = 0;
		StringBuffer feedFiller = new StringBuffer();
		StringBuffer header = new StringBuffer();
		StringBuffer note = new StringBuffer();
		StringBuffer histLine = new StringBuffer();
		DecimalFormat df1 = new DecimalFormat("######0.00");
		
		//feed apend for feed at the botom of the printer
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));
		feedFiller.append(System.getProperty("line.separator"));

		//Till USER Query
		VtiExitLdbSelectCriterion[] logSelConds =
			{
				new VtiExitLdbSelectCondition("USERID",
						VtiExitLdbSelectCondition.EQ_OPERATOR, user.getFieldValue()),
			};
        
		VtiExitLdbSelectConditionGroup logSelCondGrp = new VtiExitLdbSelectConditionGroup(logSelConds, true);
  
		VtiExitLdbTableRow[] logLdbRows = ldbLogonLdbTable.getMatchingRows(logSelCondGrp);
		//End Query

		//Build Print Date
		String formDate = date.getFieldValue();
		String printDate = "";
		printDate = formDate.substring(0,4);
		printDate = printDate + "/";
		printDate = printDate + formDate.substring(4,6);
		printDate = printDate + "/";
		printDate = printDate + formDate.substring(6,8);
		
		//Header for Printout
		header.append("Telecom Namibia Limited"); 
		header.append(System.getProperty("line.separator"));
		header.append("VAT Reg No. 10101010101");
		
		//Building the Cheq Line Items using a method of THIS class.
		for (int i = 0; i < scrTCheq.getRowCount(); ++i)
		{   
			 VtiUserExitScreenTableRow curTRow = scrTCheq.getRow(i);
			 histLine.append(makeLineItem(Long.toString(curTRow.getLongFieldValue("ORDER_NO")),
							 curTRow.getFieldValue("CHQ_NO"),
							 curTRow.getDoubleFieldValue("AMOUNT")));
		  }

		//Building Merge Fields
		VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&Header&", header.toString()),
				new VtiExitKeyValuePair("&User&", user.getFieldValue() + " : " + logLdbRows[0].getFieldValue("FIRST_NAME") + " " + logLdbRows[0].getFieldValue("LAST_NAME")),
				new VtiExitKeyValuePair("&Date&",printDate),
				new VtiExitKeyValuePair("&N10&",df1.format(currScrFN10.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&N20&",df1.format(currScrFN20.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&N50&",df1.format(currScrFN50.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&N1C&",df1.format(currScrFN100.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&N2C&",df1.format(currScrFN200.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&R10&",df1.format(currScrFR10.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&R20&",df1.format(currScrFR20.getDoubleFieldValue())),			
				new VtiExitKeyValuePair("&R50&",df1.format(currScrFR50.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&R1C&",df1.format(currScrFR100.getDoubleFieldValue())),					
				new VtiExitKeyValuePair("&R2C&",df1.format(currScrFR200.getDoubleFieldValue())),	
				new VtiExitKeyValuePair("&CN&",df1.format(currScrFNCoin.getDoubleFieldValue())),	
				new VtiExitKeyValuePair("&CR&",df1.format(currScrFRCoin.getDoubleFieldValue())),	
				new VtiExitKeyValuePair("&CFloat&",df1.format(currScrFFloat.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&TDue&",df1.format(currScrFTDue.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&Capt&",df1.format(currScrFCapt.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&Diff&",df1.format(currScrFDiff.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&TCheq&",Integer.toString(currScrFTCheq.getIntegerFieldValue())),
				new VtiExitKeyValuePair("&TChqDue&",df1.format(currScrFTCDue.getDoubleFieldValue())),
				new VtiExitKeyValuePair("&ChequeLine&", histLine.toString()),
				new VtiExitKeyValuePair("&Note&",note.toString()),
				new VtiExitKeyValuePair("&Feed&",feedFiller.toString()),
			};
					
			VtiExitKeyValuePair[] keyOpen = 
			{
			};

			//Invoking the print
			try
			{
				invokePrintTemplate("TelLogo", keyOpen);
				invokePrintTemplate("TillReconSlip", keyValuePairs);
				invokePrintTemplate("PaperCut", keyOpen);
			}
			catch (VtiExitException ee)
			{
				Log.error("Error with Printout", ee);
				return new VtiUserExitResult(999, "Printout Failed.");
			}
		return new VtiUserExitResult(999,"Recon done.");
	}
	
	private StringBuffer makeLineItem(String refDoc, String cheq ,double amnt)
	{
		StringBuffer makeLI = new StringBuffer();
		
		final String  space = " ";
		
		String lineReturn = System.getProperty("line.separator");
		int s1 = 4;
		int s2 = 1;
		int s3 = 4;
		int makeSpace = 0;
		DecimalFormat df1 = new DecimalFormat("######0.00");
		String sAmnt = df1.format(amnt);
		
		s1 = s1 - refDoc.length();
		
		if(sAmnt.length() >= 3)
			s2 = 3;
		if(sAmnt.length() >= 6)
			s2 = 2;
		if(sAmnt.length() >= 8)
			s2 = 1;
		
		makeLI.append(refDoc);
		
		while(makeSpace < 3)
		{
			makeLI.append(space);
			makeSpace++;
		}
		
		makeLI.append(cheq);
		
		makeSpace = 0;
		while(makeSpace < s2)
		{
			makeLI.append(space);
			makeSpace++;
		}	
		
		
		s3 = 32 - makeLI.length();	
		makeSpace = 0;
		while(makeSpace < s3)
		{
			makeLI.append(space);
			makeSpace++;
		}			

		makeLI.append(sAmnt);
		makeLI.append(lineReturn);
		return makeLI;
	}
	
	
}