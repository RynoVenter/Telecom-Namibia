package na.telecom.telecompos;


import java.text.DecimalFormat;
import java.util.Date;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.util.DateFormatter;
import au.com.skytechnologies.*;

public class BankDepositEnter extends VtiUserExit
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
		VtiUserExitScreenField runThis = getScreenField("SUPER");
		VtiUserExitScreenField test = getScreenField("TEST");

		//Screen table declarations
		VtiUserExitScreenTable ChequeScrTable = getScreenTable("CHEQUE");

		//LDB table declaration
		VtiExitLdbTable YSPS_TILL_CASH = getLocalDatabaseTable("YSPS_TILL_CASH");
		VtiExitLdbTable YSPS_TILL_CHEQUE = getLocalDatabaseTable("YSPS_TILL_CHEQUE");
		VtiExitLdbTable YSPS_PAYMENT_TRANSACTION = getLocalDatabaseTable("YSPS_PAYMENT_TRANSACTION");
		VtiExitLdbTable YSPS_ICMS_INV = getLocalDatabaseTable("YSPS_ICMS_INV");

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
		if (ChequeScrTable== null) return new VtiUserExitResult(999, "Screen TABLE CHEQUE does not exist");
		if (runThis == null) return new VtiUserExitResult(999, "Screen Field SUPER does not exist");
		
		if (YSPS_TILL_CASH== null) return new VtiUserExitResult(999, "LDB YSPS_TILL_CASH does not exist");
		if (YSPS_TILL_CHEQUE== null )return new VtiUserExitResult(999, "LDB YSPS_TILL_CHEQUE does not exist");
		if (YSPS_PAYMENT_TRANSACTION== null) return new VtiUserExitResult(999, "LDB YSPS_PAYMENT_TRANSACTION does not exist");
		if (YSPS_ICMS_INV== null) return new VtiUserExitResult(999, "LDB YSPS_ICMS_INV does not exist");

		if(runThis.getFieldValue().equals("X"))
			return new VtiUserExitResult(999,"Shortage/Surplus overide complete. Continue posting.");
		String strDate = TodayDate.getFieldValue();
		String strServerId = ServerId.getFieldValue();
		String strCash = Cash.getFieldValue();

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

		double dblTotalCashFromPayment = 0;
		double dblTotalCashEntered = 0;
		double dblTotalDue = 0 ;
		double dblTakeOnFloat = TakeOnFloat.getDoubleFieldValue();
		double dblDifference = 0;
		int intChequeCount = 0;
		double dblTotalChequeValue = 0;
		double dblDebit = 0;
		double dblPayment = 0;

		
		DecimalFormat df1 = new DecimalFormat("######0.00");
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
		
		
		//Check that the denominations are correct
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

		
		
		//Get total cash received 
		VtiExitLdbSelectCriterion[] cashPaymentConds =
		{
				new VtiExitLdbSelectCondition("SERVERID",	VtiExitLdbSelectCondition.EQ_OPERATOR, strServerId),
						new VtiExitLdbSelectCondition("PAY_DATE",	VtiExitLdbSelectCondition.EQ_OPERATOR, strDate),
							new VtiExitLdbSelectCondition("PURCHORDER", VtiExitLdbSelectCondition.EQ_OPERATOR, UserId.getFieldValue()),
								new VtiExitLdbSelectCondition("PAYMENT_TYPE", VtiExitLdbSelectCondition.EQ_OPERATOR, strCash),
										new VtiExitLdbSelectCondition("TIMESTAMP", VtiExitLdbSelectCondition.NE_OPERATOR, "INPROGRESS"),
		};

		VtiExitLdbSelectConditionGroup cashPaymentCondsGrp = new VtiExitLdbSelectConditionGroup(cashPaymentConds, true);

		VtiExitLdbTableRow[] cashPaymentLdbRows = YSPS_PAYMENT_TRANSACTION.getMatchingRows(cashPaymentCondsGrp);

		//Get all cheques    
		VtiExitLdbSelectCriterion[] chequePaymentConds =
		{
				new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, strServerId),
						new VtiExitLdbSelectCondition("PAY_DATE",VtiExitLdbSelectCondition.EQ_OPERATOR, strDate),
							new VtiExitLdbSelectCondition("TIMESTAMP", VtiExitLdbSelectCondition.NE_OPERATOR, "INPROGRESS"),
							  new VtiExitLdbSelectCondition("PURCHORDER", VtiExitLdbSelectCondition.EQ_OPERATOR, UserId.getFieldValue()),
								new VtiExitLdbSelectConditionGroup	(
										//Either bank cheques or personal cheques
										new VtiExitLdbSelectCriterion []
										{  
												new VtiExitLdbSelectCondition("PAYMENT_TYPE", VtiExitLdbSelectCondition.EQ_OPERATOR, "PERSONAL_CHQ"),
												new VtiExitLdbSelectCondition("PAYMENT_TYPE", VtiExitLdbSelectCondition.EQ_OPERATOR, "BANK_CHQ"),
												new VtiExitLdbSelectCondition("PAYMENT_TYPE", VtiExitLdbSelectCondition.EQ_OPERATOR, "CHEQUE"),
										},false
								)

		};

		VtiExitLdbSelectConditionGroup chequePaymentCondsGrp = new VtiExitLdbSelectConditionGroup(chequePaymentConds, true);

		VtiExitLdbTableRow[] chequePaymentLdbRows = YSPS_PAYMENT_TRANSACTION.getMatchingRows(chequePaymentCondsGrp);


		//Populate the cheque table
		ChequeScrTable.clear();
		for(int i=0; i<chequePaymentLdbRows.length; i++)
		{
			double dblAmount = chequePaymentLdbRows[i].getDoubleFieldValue("AMOUNT");
			String strChequeNo = chequePaymentLdbRows[i].getFieldValue("CHKNO");
			String strOrderNo = chequePaymentLdbRows[i].getFieldValue("VTI_REF");
			
			VtiUserExitScreenTableRow chequeScrTableRow = ChequeScrTable.getNewRow();
			chequeScrTableRow.setFieldValue("AMOUNT",df1.format(dblAmount));
			chequeScrTableRow.setFieldValue("CHQ_NO",strChequeNo);
			chequeScrTableRow.setFieldValue("ORDER_NO",strOrderNo);
			
			ChequeScrTable.appendRow(chequeScrTableRow);
			
		}
		
		
		//Sum all payments by cash
		for(int i = 0; i < cashPaymentLdbRows.length; i++)
		{
			dblPayment = cashPaymentLdbRows[i].getDoubleFieldValue("AMOUNT");
			dblTotalCashFromPayment = dblTotalCashFromPayment + dblPayment;
		}
		
		
		
		String dbt = "DEBIT";
		
		//Collect ICMS Debit Dataset
		try
        {
			VtiExitLdbSelectCriterion[] icmsDbtSelConds =
				{
					new VtiExitLdbSelectCondition("SERVER_ID", VtiExitLdbSelectCondition.EQ_OPERATOR, getVtiServerId()),
						new VtiExitLdbSelectCondition("SERVER_GROUP", VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup()),
							new VtiExitLdbSelectCondition("PAYMENT_DATE", VtiExitLdbSelectCondition.EQ_OPERATOR, TodayDate.getFieldValue()),
								new VtiExitLdbSelectCondition("USER_ID", VtiExitLdbSelectCondition.EQ_OPERATOR, UserId.getFieldValue()),
									new VtiExitLdbSelectCondition("TIMESTAMP", VtiExitLdbSelectCondition.NE_OPERATOR, "INPROGRESS"),
										new VtiExitLdbSelectCondition("PAYMENT_DEPOSIT", VtiExitLdbSelectCondition.EQ_OPERATOR, dbt)
				};
					
			VtiExitLdbSelectConditionGroup icmsDbtSelCondsGrp = new VtiExitLdbSelectConditionGroup(icmsDbtSelConds, true);
  
			VtiExitLdbTableRow [] icmsDbtLdbRows = YSPS_ICMS_INV.getMatchingRows(icmsDbtSelCondsGrp);
           
		//Sum all payments by ICMS Debit
		for(int id=0; id < icmsDbtLdbRows.length; id++)
		{
			dblDebit = dblDebit + icmsDbtLdbRows[id].getDoubleFieldValue("PAYMENT_AMT");
		}

        }
        catch (VtiExitException ee)
        {
            return new VtiUserExitResult(999, "Failed to query ICMS table.");
        }
		
		//Sum all cash entered
		dblTotalCashEntered = dblN10+dblN20+dblN50+dblN100+dblN200+dblNCoins+dblZ10+dblZ20+dblZ50+dblZ100+dblZ200+dblZCoins;


		//Calculate total due
		dblTotalDue = dblTotalCashEntered - dblTakeOnFloat;


		//Calculate the difference (Total due - captured)
		dblTotalCashFromPayment = dblTotalCashFromPayment - (dblDebit * 2);
		dblDifference = dblTotalDue - dblTotalCashFromPayment;



		//Loop at cheques table and sum up values
		for(int j=0; j<ChequeScrTable.getRowCount(); j++)
		{
			VtiUserExitScreenTableRow chequeTableRow = ChequeScrTable.getRow(j);
			double dblChequeValue = chequeTableRow.getDoubleFieldValue("AMOUNT");
			intChequeCount+=1;
			dblTotalChequeValue+=dblChequeValue;

		}


		//Assign values back to fields
		TotalCashAfterFloat.setFieldValue(df1.format(dblTotalDue));
		TotalEntered.setFieldValue(df1.format(dblTotalCashEntered));
		CashDifference.setFieldValue(df1.format(dblDifference));
		ChequeCount.setFieldValue(intChequeCount);
		ChequeDue.setFieldValue(df1.format(dblTotalChequeValue));

		
		
		return new VtiUserExitResult(000,"");

	}
}