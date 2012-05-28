package na.telecom.telecompos;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Vector;

//import com.sun.corba.se.impl.orbutil.closure.Constant;

import au.com.skytechnologies.ecssdk.log.Log;
import au.com.skytechnologies.ecssdk.util.DateFormatter;
import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitKeyValuePair;
import au.com.skytechnologies.vti.VtiExitLdbOrderSpecification;
import au.com.skytechnologies.vti.VtiExitLdbSelectCondition;
import au.com.skytechnologies.vti.VtiExitLdbSelectConditionGroup;
import au.com.skytechnologies.vti.VtiExitLdbSelectCriterion;
import au.com.skytechnologies.vti.VtiExitLdbTable;
import au.com.skytechnologies.vti.VtiExitLdbTableRow;
import au.com.skytechnologies.vti.VtiExitPrintTemplateOutput;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;
import au.com.skytechnologies.vti.VtiUserExitScreenTableRow;

public class PrintReconReport extends VtiUserExit
{
	private static int c_PrintLayoutWidth = 40;
	
	public VtiUserExitResult execute() throws VtiExitException
	{
		//LDB Definition
		VtiExitLdbTable YSPS_PAYMENT_TRANSACTION = getLocalDatabaseTable("YSPS_PAYMENT_TRANSACTION");
		VtiExitLdbTable YSPS_ICMS_INV = getLocalDatabaseTable("YSPS_ICMS_INV");
		VtiExitLdbTable YSPS_FLOAT = getLocalDatabaseTable("YSPS_FLOAT");
		
		//VTI fields definition
		VtiUserExitScreenField UserID = getScreenField("REP_ID");
		
		if(UserID==null) return new VtiUserExitResult(999,"Screen field REP_ID not found");
		if(YSPS_PAYMENT_TRANSACTION==null) return new VtiUserExitResult(999,"LDB Table YSPS_PAYMENT_TRANSACTION not found");
		if(YSPS_ICMS_INV==null) return new VtiUserExitResult(999,"LDB Table YSPS_ICMS_INV not found");
		if(YSPS_FLOAT==null) return new VtiUserExitResult(999,"LDB Table YSPS_FLOAT not found");
		
		//Data Declarations
		String strLineSeparator = System.getProperty("line.separator");
		Date currNow = new Date();
        String currDate = DateFormatter.format("yyyyMMdd", currNow);
        String currTime = DateFormatter.format("HHmmss", currNow);
		String strCurrDt = new String();
		strCurrDt = 
			String.valueOf(currDate).substring(6,8)+'/'+ 
			String.valueOf(currDate).substring(4,6)+'/'+
			String.valueOf(currDate).substring(0,4);
		String strCurrTm = new String();
		StringBuffer feedFiller= new StringBuffer();
		
		feedFiller.append(System.getProperty("line.separator"));
		
		strCurrTm = currTime.substring(0,2)+':'+ currTime.substring(2,4)+':'+currTime.substring(4,6);
		
		//Variable Declarations
		StringBuffer sbHeader = new StringBuffer();
		StringBuffer sbBody = new StringBuffer();
		StringBuffer sbFooter = new StringBuffer();
		Vector v = new Vector();
		String strCurrPaymentType = new String();
		String strTwoSpaces = "  ";
		int intCurrPosition = 0;
		String strFiller = "          ";
		double dblGrandTotal = 0;
		double dblSubTotal = 0;
		double dblDebit = 0;
		double dblFloat = 0;
		double dblGRNDFloat = 0;
		double dblGRNDDebit = 0;
		
		DecimalFormat df1 = new DecimalFormat("$######0.00");
		
		String strMessage = new String();
		String strLine    = "****************************************";
		strMessage = "Reconciliation report";
		
		AddCentre(this, sbHeader, strMessage, c_PrintLayoutWidth);
		
		sbHeader.append(strLineSeparator);
		sbHeader.append(strLineSeparator);
		strMessage = "Date : "+ strCurrDt;
		sbHeader.append(strMessage);
		sbHeader.append(strLineSeparator);
		strMessage = "Time : "+ strCurrTm;
		sbHeader.append(strMessage);
		sbHeader.append(strLineSeparator);
		strMessage = "User : "+ getVtiServerId();
		sbHeader.append(strMessage);
		sbHeader.append(strLineSeparator);
		sbHeader.append(strLine);
		sbHeader.append(strLineSeparator);
		
		
		//Collect Float Dataset
			try
			{
				VtiExitLdbSelectCriterion[] floatSelConds =
			{
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, UserID.getFieldValue()),
							new VtiExitLdbSelectCondition("ONDATE", VtiExitLdbSelectCondition.EQ_OPERATOR, currDate),
			};
					
			VtiExitLdbSelectConditionGroup floatSelCondsGrp = new VtiExitLdbSelectConditionGroup(floatSelConds, true);
  
			VtiExitLdbTableRow [] floatLdbRows = YSPS_FLOAT.getMatchingRows(floatSelCondsGrp);		
			if(	floatLdbRows.length == 0)
				return new VtiUserExitResult(999, "Failed to query Float table.");
			dblFloat = floatLdbRows[0].getDoubleFieldValue("CASH");
			dblGRNDFloat = floatLdbRows[0].getDoubleFieldValue("CASH");
							
			}
			catch (VtiExitException ee)
        	{
            	return new VtiUserExitResult(999, "Failed to query Float table.");
       		}
			
			
			//Get ICMS Debit
							try
							{
								VtiExitLdbSelectCriterion[] icmsDbtSelConds =
								{
									new VtiExitLdbSelectCondition("SERVER_ID", VtiExitLdbSelectCondition.EQ_OPERATOR, getVtiServerId()),
										new VtiExitLdbSelectCondition("SERVER_GROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
											new VtiExitLdbSelectCondition("PAYMENT_DATE", VtiExitLdbSelectCondition.EQ_OPERATOR, currDate),
												new VtiExitLdbSelectCondition("USER_ID", VtiExitLdbSelectCondition.EQ_OPERATOR, UserID.getFieldValue()),
													new VtiExitLdbSelectCondition("TIMESTAMP", VtiExitLdbSelectCondition.NE_OPERATOR, "INPROGRESS"),
														new VtiExitLdbSelectCondition("PAYMENT_DEPOSIT", VtiExitLdbSelectCondition.EQ_OPERATOR, "DEBIT"),
															new VtiExitLdbSelectCondition("PAYMENT_TYPE", VtiExitLdbSelectCondition.EQ_OPERATOR, "CASH")
							};
					
							VtiExitLdbSelectConditionGroup icmsDbtSelCondsGrp = new VtiExitLdbSelectConditionGroup(icmsDbtSelConds, true);
  
							VtiExitLdbTableRow [] icmsDbtLdbRows = YSPS_ICMS_INV.getMatchingRows(icmsDbtSelCondsGrp);
           
							//Sum all payments by ICMS Debit
							for(int id=0; id < icmsDbtLdbRows.length; id++)
							{
								dblDebit = dblDebit + icmsDbtLdbRows[id].getDoubleFieldValue("PAYMENT_AMT");
							}
								dblGRNDDebit = dblDebit;
        					}
       						catch (VtiExitException ee)
        					{
            					return new VtiUserExitResult(999, "Failed to query ICMS table.");
       						}
		//Get destination
		VtiExitLdbSelectCriterion [] selTransactionsConds =
		{
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
							new VtiExitLdbSelectCondition("TIMESTAMP", VtiExitLdbSelectCondition.NE_OPERATOR, "INPROGRESS"),
									new VtiExitLdbSelectCondition("PAY_DATE", VtiExitLdbSelectCondition.EQ_OPERATOR, currDate),
											new VtiExitLdbSelectCondition("PURCHORDER", VtiExitLdbSelectCondition.EQ_OPERATOR, UserID.getFieldValue()),
		};

		VtiExitLdbSelectConditionGroup selTransactionsCondsGroup =  new VtiExitLdbSelectConditionGroup(selTransactionsConds,true);
		
		VtiExitLdbOrderSpecification selTransSortByPaymentType = new VtiExitLdbOrderSpecification("PAYMENT_TYPE",true);
		
		VtiExitLdbTableRow [] paymentRows = YSPS_PAYMENT_TRANSACTION.getMatchingRows(selTransactionsCondsGroup,selTransSortByPaymentType);
		
		if(paymentRows.length==0)
		{
			strMessage = "There is no transaction found";
			sbBody.append(strMessage);
			sbBody.append(strLineSeparator);
			sbBody.append(strLineSeparator);
		}
		else
		{
			//Process records
			for(int i=0; i<paymentRows.length; i++)
			{	
				String strSelPaymentType = paymentRows[i].getFieldValue("PAYMENT_TYPE");
				String strSelOrderNo = paymentRows[i].getFieldValue("VTI_REF");
				String strSelPaySeq = paymentRows[i].getFieldValue("PAYMENT_SEQ");
				String strChequeNo  = paymentRows[i].getFieldValue("CHKNO");
				String strVoucherNo  = paymentRows[i].getFieldValue("VOUCHER_NO");
				double dblAmount = paymentRows[i].getDoubleFieldValue("AMOUNT");
				
				if(!strCurrPaymentType.equals(strSelPaymentType))
				{
					if(!strCurrPaymentType.equals(""))
					{
						strMessage = strLine;
						sbBody.append(strMessage);
						sbBody.append(strLineSeparator);
						strMessage = "Total                        " + df1.format(dblSubTotal);
						sbBody.append(strMessage);
						sbBody.append(strLineSeparator);
						strMessage = strLine;
						sbBody.append(strMessage);
						sbBody.append(strLineSeparator);
					}
					
					strCurrPaymentType = strSelPaymentType;
					sbBody.append(strCurrPaymentType);
					sbBody.append(strLineSeparator);
					dblSubTotal = 0;
				}
				
				intCurrPosition = strSelOrderNo.length();
				while(intCurrPosition<6)
				{
					strSelOrderNo = " "+strSelOrderNo;
					intCurrPosition = strSelOrderNo.length();
				}
				
				intCurrPosition = strSelPaySeq.length();
				while(intCurrPosition<2)
				{
					strSelPaySeq= " "+strSelPaySeq;
					intCurrPosition = strSelPaySeq.length();
				}
				
				intCurrPosition = strChequeNo.length();
				while(intCurrPosition<10)
				{
					strChequeNo= " "+strChequeNo;
					intCurrPosition = strChequeNo.length();
				}
				
				intCurrPosition = strVoucherNo.length();
				while(intCurrPosition<10)
				{
					strVoucherNo= " "+strVoucherNo;
					intCurrPosition = strVoucherNo.length();
				}
				
				
				
				if(strCurrPaymentType.equals("CASH"))
				{
					//strMessage = strSelOrderNo+"/"+strSelPaySeq+strChequeNo+strFiller+df1.format(dblAmount);
					//sbBody.append(strMessage);
					//sbBody.append(strLineSeparator);
							
					
					dblSubTotal+=dblAmount - (dblDebit * 2) + dblFloat;
					dblDebit = 0;
					dblFloat = 0;
				}
				else if(strCurrPaymentType.equals("CREDIT_CARD"))
				{
					//strMessage = strSelOrderNo+"/"+strSelPaySeq+strChequeNo+strFiller+df1.format(dblAmount);
					//sbBody.append(strMessage);
					//sbBody.append(strLineSeparator);
					dblSubTotal+=dblAmount;
	
				}
				else if(strCurrPaymentType.equals("PERSONAL_CHQ"))
				{
					//strMessage = strSelOrderNo+"/"+strSelPaySeq+strChequeNo+strFiller+df1.format(dblAmount);
					//sbBody.append(strMessage);
					//sbBody.append(strLineSeparator);
					dblSubTotal+=dblAmount;
	
				}
				else if(strCurrPaymentType.equals("GIFT_VOUCHER"))
				{
					//strMessage = strSelOrderNo+"/"+strSelPaySeq+strVoucherNo+strFiller+df1.format(dblAmount);
					//sbBody.append(strMessage);
					//sbBody.append(strLineSeparator);
					dblSubTotal+=dblAmount;
	
				}
				else if(strCurrPaymentType.equals("DEBIT_CARD"))
				{
					//strMessage = strSelOrderNo+"/"+strSelPaySeq+strChequeNo+strFiller+df1.format(dblAmount);
					//sbBody.append(strMessage);
					//sbBody.append(strLineSeparator);
					dblSubTotal+=dblAmount;
	
				}
				else if(strCurrPaymentType.equals("BANK_CHQ"))
				{
					//strMessage = strSelOrderNo+"/"+strSelPaySeq+strChequeNo+strFiller+df1.format(dblAmount);
					//sbBody.append(strMessage);
					//sbBody.append(strLineSeparator);
					dblSubTotal+=dblAmount;
	
				}
				else if(strCurrPaymentType.equals("CHEQUE"))
				{
					//strMessage = strSelOrderNo+"/"+strSelPaySeq+strChequeNo+strFiller+df1.format(dblAmount);
					//sbBody.append(strMessage);
					//sbBody.append(strLineSeparator);
					dblSubTotal+=dblAmount;
	
				}
				
				dblGrandTotal+=dblAmount;
				
			}
		}
		
		
		if(dblSubTotal!=0)
		{
			strMessage = strLine;
			sbBody.append(strMessage);
			sbBody.append(strLineSeparator);
			if(strCurrPaymentType.equals("CASH"))
			   dblSubTotal = dblSubTotal  - dblGRNDDebit;

			strMessage = "Total                        " + df1.format(dblSubTotal);
			sbBody.append(strMessage);
			sbBody.append(strLineSeparator);
			strMessage = strLine;
			sbBody.append(strMessage);
			sbBody.append(strLineSeparator);
		}
		
		//Footer to display grandTotal
		sbFooter.append(strLine);
		sbFooter.append(strLineSeparator);
		strMessage = "Grand Total                  " + df1.format(dblGrandTotal + dblGRNDFloat - (dblGRNDDebit * 2));
		sbFooter.append(strMessage);
		sbFooter.append(strLineSeparator);
		sbFooter.append(strLineSeparator);
		sbFooter.append(strLine);
		sbFooter.append(strLineSeparator);
		sbFooter.append(feedFiller);
		
		v.addElement(new VtiExitKeyValuePair("HEADER", sbHeader.toString()));
		v.addElement(new VtiExitKeyValuePair("BODY", sbBody.toString()));
		v.addElement(new VtiExitKeyValuePair("FOOTER", sbFooter.toString()));
		
		VtiExitPrintTemplateOutput printOutput = null;
		
		VtiExitKeyValuePair [] substVars = new VtiExitKeyValuePair[v.size()];
		v.copyInto(substVars);
		
		try
		{
			printOutput =  VtiUserExit.invokePrintTemplate("ReconReport", substVars);
			
			if(printOutput==null)
			{
				return new VtiUserExitResult(999, "Error invoking print template");
			}
		}
		catch(VtiExitException e)
		{
			return new VtiUserExitResult(999, e.getMessage());
		}
		//makeCardSummary();
		return new VtiUserExitResult();
	}
	
	private void AddCentre( VtiUserExit _exit, StringBuffer sbMessage,  String strMessage,  int intMaxColumn) throws VtiExitException
	{
		int intMidPoint = 0;
		int intOffset = 0;
		int intStrLen = strMessage.length();
		int intCurrPosition = 0;
		String strNewMessage = new String();
		
		if(intMaxColumn==0)
			throw new VtiExitException("Maximum column for printing is not defined");
		
		intMidPoint = intMaxColumn /2;
		intOffset = intMidPoint - (intStrLen/2);
		
	    if(intOffset>0)
	    {
	    	while(intCurrPosition!=intOffset)
	    	{
	    		strNewMessage = strNewMessage + " ";
	    		intCurrPosition+=1;
	    		
	    	}
	    }
		strNewMessage = strNewMessage + strMessage;
		
		sbMessage.append(strNewMessage);
		
		return;
	}
	
	
private void AddPaymentType( VtiUserExit _exit,  StringBuffer sbMessage,  String strMessage,  int intMaxColumn) throws VtiExitException
{
int intMidPoint = 0;
int intOffset = 0;
int intStrLen = strMessage.length();
int intCurrPosition = 0;
String strNewMessage = new String();

if(intMaxColumn==0)
throw new VtiExitException("Maximum column for printing is not defined");

intMidPoint = intMaxColumn /2;
intOffset = intMidPoint - (intStrLen/2);

if(intOffset>0)
{
while(intCurrPosition!=intOffset)
{
strNewMessage = strNewMessage + " ";
intCurrPosition+=1;

}
}
strNewMessage = strNewMessage + strMessage;

sbMessage.append(strNewMessage);

return ;
}
	
	
	//Class Methods
	
	public VtiUserExitResult makeCardSummary() throws VtiExitException
	{
		//LDB Definition
		VtiExitLdbTable ldbDocMat = getLocalDatabaseTable("YSPS_DOC_ITEMS");
		VtiExitLdbTable ldbMatMast = getLocalDatabaseTable("YSPS_MATERIAL");
		VtiExitLdbTable ldbPayTran = getLocalDatabaseTable("YSPS_PAYMENT_TRANSACTION");
		VtiExitLdbTable ldbFloat = getLocalDatabaseTable("YSPS_FLOAT");
		
		if(ldbDocMat==null) return new VtiUserExitResult(999,"LDB Table YSPS_DOC_ITEMS not found");
		if(ldbMatMast==null) return new VtiUserExitResult(999,"LDB Table YSPS_MATERIAL not found");
		if(ldbPayTran==null) return new VtiUserExitResult(999,"LDB Table YSPS_PAYMENT_TRANSACTION not found");
		if(ldbFloat==null) return new VtiUserExitResult(999,"LDB Table YSPS_FLOAT not found");
		
		//VTI fields definition
		VtiUserExitScreenField scFUserID = getScreenField("REP_ID");
		
		if(scFUserID==null) return new VtiUserExitResult(999,"Screen field REP_ID not found");
		
		//Attribute Declarations
		Date currNow = new Date();
        String currDate = DateFormatter.format("yyyyMMdd", currNow);
		StringBuffer cards = new StringBuffer();
		long soldQty = 0;
		long issQty = 0;
		String ean = "";
		String matNr = "";
		String cardDesc = "";
		String lineReturn = System.getProperty("line.separator");
		StringBuffer feedFiller = new StringBuffer();
		
		//Collect the days  transactions Dataset for the user
		VtiExitLdbSelectCriterion [] selPayTransConds =
		{
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
					new VtiExitLdbSelectCondition("PAYMENT_SEQ", VtiExitLdbSelectCondition.EQ_OPERATOR, "1"),
						new VtiExitLdbSelectCondition("TIMESTAMP", VtiExitLdbSelectCondition.NE_OPERATOR, "INPROGRESS"),
							new VtiExitLdbSelectCondition("PAY_DATE", VtiExitLdbSelectCondition.EQ_OPERATOR, currDate),
								new VtiExitLdbSelectCondition("PURCHORDER", VtiExitLdbSelectCondition.EQ_OPERATOR, scFUserID.getFieldValue()),
		};

		VtiExitLdbSelectConditionGroup selPayTransCondsGroup =  new VtiExitLdbSelectConditionGroup(selPayTransConds,true);
		
		VtiExitLdbTableRow [] payTransRows = ldbPayTran.getMatchingRows(selPayTransCondsGroup);
		
		if(payTransRows.length == 0) return new VtiUserExitResult(999,"No payment transactions for this POS");
		
		//Collect Material Dataset where MATKL is equal to 19
		VtiExitLdbSelectCriterion [] selMatMastConds =
		{
				new VtiExitLdbSelectCondition("MATKL", VtiExitLdbSelectCondition.EQ_OPERATOR, "19"),					
		};

		VtiExitLdbSelectConditionGroup selMatMastCondsGroup =  new VtiExitLdbSelectConditionGroup(selMatMastConds,true);
		
		VtiExitLdbTableRow [] matMastRows = ldbMatMast.getMatchingRows(selMatMastCondsGroup);
		
		if(matMastRows.length == 0) return new VtiUserExitResult(999,"No Card items in the Material LDB.");
		
		for(int iMat = 0 ; iMat < matMastRows.length ; iMat++)
		{
			ean  = matMastRows[iMat].getFieldValue("EAN");
			matNr  = matMastRows[iMat].getFieldValue("MATERIAL");
			cardDesc = matMastRows[iMat].getStringFieldValue("MAT_DESC");
			soldQty = 0;
			issQty = 0;

						
						//Collect Doc Items Dataset one by one per Vti Ref from PayTran
						VtiExitLdbSelectCriterion [] selFloatConds =
						{
								new VtiExitLdbSelectCondition("ONDATE", VtiExitLdbSelectCondition.EQ_OPERATOR, currDate),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
										new VtiExitLdbSelectCondition("MATNR", VtiExitLdbSelectCondition.EQ_OPERATOR, matNr),
						};

						VtiExitLdbSelectConditionGroup selFloatCondsGroup =  new VtiExitLdbSelectConditionGroup(selFloatConds,true);
		
						VtiExitLdbTableRow [] floatRows = ldbFloat.getMatchingRows(selFloatCondsGroup);
						
						if(floatRows.length == 0) 
							issQty = 0;
						else
							issQty = floatRows[0].getLongFieldValue("QTY");
						
			for(int iTran = 0 ; iTran < payTransRows.length ; iTran++)
			{
						//Collect Doc Items Dataset one by one per Vti Ref from PayTran
						VtiExitLdbSelectCriterion [] selDocItemsConds =
						{
								new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, payTransRows[iTran].getFieldValue("VTI_REF")),
									new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerId()),
						};

						VtiExitLdbSelectConditionGroup selDocItemsCondsGroup =  new VtiExitLdbSelectConditionGroup(selDocItemsConds,true);
		
						VtiExitLdbTableRow [] docItemsRows = ldbDocMat.getMatchingRows(selDocItemsCondsGroup);
						
						if(docItemsRows.length == 0) return new VtiUserExitResult(999,"No items in Document items LDB for this Vti Ref.");

						for(int iAppend = 0 ; iAppend < docItemsRows.length ; iAppend++)
						{
							if(docItemsRows[iAppend].getFieldValue("MATERIAL").equals(ean) || docItemsRows[iAppend].getFieldValue("MATERIAL").equals(matNr))
								soldQty = soldQty + docItemsRows[iAppend].getLongFieldValue("ITEM_QTY");
						}
			}
			
			if(soldQty > 0 || issQty > 0)
			{
				cards.append("------------------------------------");
				cards.append(lineReturn);
				if(cardDesc.length() <= 35)
					cards.append(cardDesc.substring(0,cardDesc.length()));
				else
					cards.append(cardDesc.substring(0,35));
				cards.append(lineReturn);
				cards.append("Iss : ");
				cards.append(issQty);	
				cards.append("   ");
				cards.append("Sold : ");
				cards.append(soldQty);
				cards.append("  Till : ");
				cards.append(issQty - soldQty);
				cards.append(lineReturn);
			}
		}
				feedFiller.append(System.getProperty("line.separator"));
				feedFiller.append(System.getProperty("line.separator"));
				feedFiller.append(System.getProperty("line.separator"));
				feedFiller.append(System.getProperty("line.separator"));
				feedFiller.append(System.getProperty("line.separator"));
				feedFiller.append(System.getProperty("line.separator"));
				feedFiller.append(System.getProperty("line.separator"));
				feedFiller.append(System.getProperty("line.separator"));
		
			VtiExitKeyValuePair[] keyValuePairs = 
			{
				new VtiExitKeyValuePair("&HEADER&", "CARD QTY SUMMARY"),
				new VtiExitKeyValuePair("&BODY&", cards.toString()),
				new VtiExitKeyValuePair("&FOOTER&", feedFiller.toString()),
			};
			
			VtiExitKeyValuePair[] keyOpen = 
			{
			};
			
			try
			{
				invokePrintTemplate("ReconReport", keyValuePairs);
				invokePrintTemplate("PaperCut", keyOpen);
			}
			catch (VtiExitException ee)
			{
			}   
		return new VtiUserExitResult();
	}
	
private StringBuffer makeLineItem(String matNum,int qty,double disc,double val,String matDesc)
	{
		StringBuffer makeLI = new StringBuffer();
		
		final String  space = " ";
		
		String lineReturn = System.getProperty("line.separator");
		int s1 = 4;
		int s2 = 1;
		int s3 = 4;
		int makeSpace = 0;
		String qtyS = Integer.toString(qty);
		
		DecimalFormat df1 = new DecimalFormat("######0.00");
		String discD = df1.format(disc);
		s1 = s1 - qtyS.length();
		
		if(discD.length() >= 3)
			s2 = 3;
		if(discD.length() >= 6)
			s2 = 2;
		if(discD.length() >= 8)
			s2 = 1;
		
		makeLI.append(matNum);
		
		while(makeSpace < s1)
		{
			makeLI.append(space);
			makeSpace++;
		}
		
		makeLI.append(qty);
		
		makeSpace = 0;
		while(makeSpace < s2)
		{
			makeLI.append(space);
			makeSpace++;
		}	
		
		makeLI.append(df1.format(disc));
		
		s3 = 32 - makeLI.length();	
		makeSpace = 0;
		while(makeSpace < s3)
		{
			makeLI.append(space);
			makeSpace++;
		}			
		makeLI.append(df1.format(val));
		makeLI.append(lineReturn);
		makeLI.append(matDesc);
		makeLI.append(lineReturn);
		
		return makeLI;
	}
	
	private StringBuffer makeLineItem(String matNum,int qty,String matDesc)
	{
		StringBuffer makeLI = new StringBuffer();
		String lineReturn = System.getProperty("line.separator");
		String qtyS = Integer.toString(qty);
		
		makeLI.append(matNum);
		makeLI.append("  ");
		makeLI.append(qtyS);
		makeLI.append(lineReturn);
		makeLI.append(matDesc);
		makeLI.append(lineReturn);
			
		return makeLI;
	}
	
	

}
	