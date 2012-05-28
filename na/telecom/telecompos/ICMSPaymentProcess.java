package na.telecom.telecompos;

import java.util.Hashtable;
import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

//This exit is called to process the payment.

public class ICMSPaymentProcess
extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Get Screen Fields, Tables and LDB's we will need and check that they exist.

		VtiUserExitScreenField orderNoField = getScreenField("ORDER_NO");
		VtiUserExitScreenField totalOwingField = getScreenField("TOTAL_OWING");
		VtiUserExitScreenField payAmountField = getScreenField("PAY_AMOUNT");
		VtiUserExitScreenField totalChangeField = getScreenField("TOTAL_CHANGE");
		VtiUserExitScreenField totalPaidField = getScreenField("TOTAL_PAID");
		VtiUserExitScreenField accNo = getScreenField("ACC_NUM");
		VtiUserExitScreenField accNam = getScreenField("ACC_NAME");
		VtiUserExitScreenField branch = getScreenField("BRANCH");
		VtiUserExitScreenField telNum = getScreenField("TEL_NUM");
		VtiUserExitScreenField ChkNoField = getScreenField("CHEQUE_NO");
		VtiUserExitScreenField ccAuthField = getScreenField("CREDITCARD");
		
		VtiUserExitScreenField rbCashField = getScreenField("R_CASH");
		VtiUserExitScreenField rbEftDebitField = getScreenField("R_EFT_DEBIT");
		VtiUserExitScreenField rbEftCreditField = getScreenField("R_EFT_CREDIT");
		VtiUserExitScreenField rbEftposField = getScreenField("R_EFTPOS");
		VtiUserExitScreenField rbPerCheqField = getScreenField("R_PER_CHEQUE");
		VtiUserExitScreenField rbBankCheqField = getScreenField("R_BANK_CHEQUE");


		VtiUserExitScreenField docTypeField = getScreenField("DOC_TYPE");
		VtiUserExitScreenField orderTotalField = getScreenField("ORDER_TOTAL");
		VtiUserExitScreenField ccTypeField = getScreenField("CC_TYPE");
//		VtiUserExitScreenField ccAuthField = getScreenField("CC_AUTH");

		if (ccAuthField == null) return new VtiUserExitResult(999, "Field CREDITCARD not found");
		if (orderNoField == null) return new VtiUserExitResult(999, "Field VTI_REF not found");
		if (totalOwingField == null) return new VtiUserExitResult(999, "Field TOTAL_OWING not found");
		if (payAmountField == null) return new VtiUserExitResult(999, "Field PAY_AMOUNT not found");
		if (totalChangeField == null) return new VtiUserExitResult(999, "Field TOTAL_CHANGE not found");
		if (totalPaidField == null) return new VtiUserExitResult(999, "Field TOTAL_PAID not found");
		if (rbCashField == null) return new VtiUserExitResult(999, "Field R_CASH not found");
		if (rbEftDebitField == null) return new VtiUserExitResult(999, "Field R_EFT_DEBIT not found");
		if (rbEftCreditField == null) return new VtiUserExitResult(999, "Field R_EFT_CREDIT not found");
		if (rbEftposField == null) return new VtiUserExitResult(999, "Field R_EFTPOS not found");
		if (rbPerCheqField == null) return new VtiUserExitResult(999, "Field R_PER_CHEQUE not found");
		if (rbBankCheqField == null) return new VtiUserExitResult(999, "Field R_BANK_CHEQUE not found");
		if (docTypeField == null) return new VtiUserExitResult(999, "Field DOC_TYPE not found");
		if (orderTotalField == null) return new VtiUserExitResult(999, "Field ORDER_TOTAL not found");
		if (ccTypeField == null) return new VtiUserExitResult(999, "Field CC_TYPE not found");
//		if (ccAuthField == null)
//			return new VtiUserExitResult(999, "Field CC_AUTH not found");

		// Setup LDB's that we will use
		VtiExitLdbTable paymentLdbTable = getLocalDatabaseTable("YSPS_PAYMENT_TRANSACTION");

		VtiExitLdbTable docHeaderLdbTable = getLocalDatabaseTable("YSPS_ICMS_INV");

		if (paymentLdbTable == null)
			return new VtiUserExitResult (999, "LDB table YSPS_PAYMENT_TRANSACTION not found");
		if (docHeaderLdbTable == null)
			return new VtiUserExitResult (999, "LDB table YSPS_ICMS_INV not found");


		// Get the Screen Table Definition.

		VtiUserExitScreenTable paymentHistory = getScreenTable("HISTORY");

		if (paymentHistory == null)
			return new VtiUserExitResult (999, "Screen table HISTORY not found");

		// Get the session header information.
		VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();

		if (sessionHeader == null)
			return new VtiUserExitResult(999, "Error Retrieving Session Header Info");

		String orderNo = orderNoField.getFieldValue();
		String vtiServerId = getVtiServerId();
		double payAmount = payAmountField.getDoubleFieldValue();
		double totalOwing = totalOwingField.getDoubleFieldValue();
		double totalPaid = totalPaidField.getDoubleFieldValue();
		double totalChange = totalChangeField.getDoubleFieldValue();
		double orderTotal = orderTotalField.getDoubleFieldValue();
		String currDate = DateFormatter.format("yyyyMMdd");
		String currTime = DateFormatter.format("HHmmss");
		String paymentDesc = "";
		String payType = "";

		String docType = docTypeField.getFieldValue();
		String voucherNo = "";
		boolean openCashDrawer = true;
		int deviceId = sessionHeader.getDeviceNumber();
		String ccType = ccTypeField.getFieldValue();
//		String ccAuth = ccAuthField.getFieldValue();
		String rbCash = rbCashField.getFieldValue();

		// Check that a payment has been entered i.e. it is not zero.
		if (payAmount <= 0)
		{
			setCursorPosition(payAmountField);
			return new VtiUserExitResult(999, "Payment must be Greater than zero");
		}    

		// Determine the Payment Type by checking each of the active radio buttons for a value of "X"
		// Note that 2 pay type variables are used, one for printing, and one for update on LDB.

		if (rbCashField.getFieldValue().equals("X"))
		{
			ccType = "";
//			ccAuth = "";
			voucherNo = "";
			openCashDrawer = true;
			paymentDesc = "Cash";
			payType = "CASH";
			if (payAmount < -100.0)
			{
				return new VtiUserExitResult(999, "Refunds Can Not Exceed $100 Cash");
			}
		}



		if (rbEftDebitField.getFieldValue().equals("X"))
		{
			ccType = "";
			//ccAuth = "";
			voucherNo = "";
			openCashDrawer = false;
			payType = "DEBIT_CARD";
			paymentDesc = "Debit Card";
			if (ccAuthField.getFieldValue().equals(""))
			  {
				 setCursorPosition(ccAuthField);
		      		return new VtiUserExitResult(999, "Please enter the card number.");
			  }
		}

		if (rbEftCreditField.getFieldValue().equals("X"))
		{
			payType = "CREDIT_CARD";
			paymentDesc = "Credit Card";
			voucherNo = "";
			openCashDrawer = false;
			
			if (ccAuthField.getFieldValue().equals(""))
			  {
				 setCursorPosition(ccAuthField);
		      		return new VtiUserExitResult(999, "Enter the last 4 digits of the Credit Card.");
			  }
		}

		if (rbEftposField.getFieldValue().equals("X"))
		{
			ccType = "";
			////ccAuth = "";
			voucherNo = "";
			openCashDrawer = false;
			payType = "EFTPOS";
			paymentDesc = "Eftpos";
		}

		if (rbPerCheqField.getFieldValue().equals("X"))
		{
			ccType = "";
			////ccAuth = "";
			voucherNo = "";
			openCashDrawer = true;

		      if (ChkNoField.getFieldValue().equals(""))
			  {
				 setCursorPosition(ChkNoField);
		      	 return new VtiUserExitResult(999, "Please enter the cheque number.");
			  }
			  if (accNo.getFieldValue().equals(""))
			  {
				 setCursorPosition(accNo);
		      	 return new VtiUserExitResult(999, "Please enter the account number.");
			  }
			  if (accNam.getFieldValue().equals(""))
			  {
				 setCursorPosition(accNam);
		      	 return new VtiUserExitResult(999, "Please enter the name of the bank account");
			  }
			  if (branch.getFieldValue().equals(""))
			  {
				 setCursorPosition(branch);
		      	 return new VtiUserExitResult(999, "Please enter the branch issueing the cheque.");
			  }
			  if (telNum.getFieldValue().equals(""))
			  {
				 setCursorPosition(telNum);
		      	 return new VtiUserExitResult(999, "Please enter the customers telephone number.");
			  }
			
			payType = "PERSONAL_CHQ";
			paymentDesc = "Cheque";
			
		}

		if (rbBankCheqField.getFieldValue().equals("X"))
		{
			ccType = "";
			////ccAuth = "";
			voucherNo = "";
			
		      if (ChkNoField.getFieldValue().equals(""))
			  {
				 setCursorPosition(ChkNoField);
		      	 return new VtiUserExitResult(999, "Please enter the cheque number.");
			  }
			  if (accNo.getFieldValue().equals(""))
			  {
				 setCursorPosition(accNo);
		      	 return new VtiUserExitResult(999, "Please enter the account number.");
			  }
			  if (accNam.getFieldValue().equals(""))
			  {
				 setCursorPosition(accNam);
		      	 return new VtiUserExitResult(999, "Please enter the name of the bank account");
			  }
			  if (branch.getFieldValue().equals(""))
			  {
				 setCursorPosition(branch);
		      	 return new VtiUserExitResult(999, "Please enter the branch issueing the cheque.");
			  }
			  if (telNum.getFieldValue().equals(""))
			  {
				 setCursorPosition(telNum);
		      	 return new VtiUserExitResult(999, "Please enter the customers telephone number.");
			  }
			
			
			
			payType = "BANK_CHQ";
			paymentDesc = "Bank Cheque";
			openCashDrawer = true;
		}



		// Select the doc header record as it will be modified with the updated
		// payment totals.

		VtiExitLdbSelectCriterion[] headerSelConds =
		{
				new VtiExitLdbSelectCondition("REFERENCE_NO", VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
						new VtiExitLdbSelectCondition("SERVER_ID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
		};

		VtiExitLdbSelectConditionGroup headerSelCondGrp = new VtiExitLdbSelectConditionGroup(headerSelConds, true);

		// Fetch the corresponding items from the DOC_HEADER
		// LDB table.

		VtiExitLdbTableRow[] docHeaderLdbRows = docHeaderLdbTable.getMatchingRows(headerSelCondGrp);

		// Check that the order was found.
		if (docHeaderLdbRows.length == 0)
			return new VtiUserExitResult(999, "Order Not Found");



		// Check that there is an amount outstanding before proceeding.
		totalOwing = orderTotal - totalPaid;

		if (totalOwing <= 0.02)
			return new VtiUserExitResult(999, "Order is Fully Paid.");

		// If the totalOwing is Less than (or Equal) zero, we will be giving change, hence we need
		// to adjust the "Payment" to be the actual value of money kept by us.  I.e the payment
		// less any change given.  Also we can assume that the order is now complete, hence the status can be
		// updated to reflect this.

		// If the payment type is cash, we will need to perform some rounding to the total owing
		// before calculating change etc.

		if (rbCash.equals("X"))
		{
			//If a cash payment, check the "pay amount" is valid.
			if (rbCash.equals("X"))
			{
				int intPayAmount = (int) Math.round( (payAmount * 100)) % 5;
				if (! (intPayAmount == 0))
				{
					setCursorPosition(payAmountField);
					return new VtiUserExitResult(999, "Invalid Pay Amount for a cash payment");
				}
			}

			int intTotalOwing = (int) Math.round( (totalOwing * 100)) % 5;

			switch (intTotalOwing)
			{
			case 0:
			{
				break;
			}
			case 1:
			{
				totalOwing -= 0.01;
				Log.info("New Total Owing " + totalOwing);
			}
			break;

			case 2:
			{
				totalOwing -= 0.02;
			}
			break;

			case 3:
			{
				totalOwing += 0.02;
			}
			break;

			case 4:
			{
				totalOwing += 0.01;
			}
			break;
			}

			// Calculate the total change.
			totalChange = payAmount - totalOwing;
			totalChange = ( (double) Math.round(totalChange * 100)) / 100;
		}

		// Other Payment Types.
		else
		{
			//Check that the pay amount will not exceed the total outstanding.  Only for payment
			//types other than cash.
			totalChange = payAmount - totalOwing;

			if ( (docType.equals("SALE")) && (totalChange > 0))
			{
				return new VtiUserExitResult(999, "Pay Amount exceeds outstanding amount");
			}
		}

		totalPaid += payAmount;

		//if ((totalChange > 0.02) && (payType.equals("CASH")))  // Not sure why we don't give change for other types??
		if ( (totalChange >= 0.00) && (payType.equals("CASH")))
		{
			totalPaid = totalPaid - totalChange;
			payAmount = payAmount - totalChange;
			totalOwing = 0;

			// Perform a rounding check.
			int intTotalChange = (int) Math.round( (totalChange * 100)) % 5;

			switch (intTotalChange)
			{
			case 0:
				break;

			case 1:
			{
				totalChange -= 0.01;
			}
			break;

			case 2:
			{
				totalChange -= 0.02;
			}
			break;

			case 3:
			{
				totalChange += 0.02;
			}
			break;

			case 4:
			{
				totalChange += 0.01; ;
			}
			break;
			}

		}

		else
		{
			totalChange = 0;
			totalOwing = orderTotal - totalPaid;
		}

		// Build the selection criteria to retrieve
		// Existing Payments.

		VtiExitLdbSelectCriterion[] paymentSelConds =
		{
				new VtiExitLdbSelectCondition("VTI_REF",
						VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
						new VtiExitLdbSelectCondition("SERVERID",
								VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
		};

		VtiExitLdbSelectConditionGroup paymentSelCondGrp =
			new VtiExitLdbSelectConditionGroup(paymentSelConds, true);

		// Fetch the corresponding items from the Payment_transactions
		// LDB table.
		VtiExitLdbTableRow[] paymentLdbRows =
			paymentLdbTable.getMatchingRows(paymentSelCondGrp);

		// Check to see if any rows were returned.  If they were the next Row will have a
		// sequence number of +1.

		int paymentSequence = 1;
		int noOfPayments = paymentLdbRows.length;

		if (noOfPayments > 0)
		{
			paymentSequence = noOfPayments + 1;

			// create a new row for saving

		}
		VtiExitLdbTableRow newPayment = paymentLdbTable.newRow();

		String refDoc = "";

	   
	    if (rbEftDebitField.getFieldValue().equals("X"))
	    {
			    newPayment.setFieldValue("REFDOC", ccAuthField.getFieldValue());
				refDoc = ccAuthField.getFieldValue();
	    }

	    if (rbEftCreditField.getFieldValue().equals("X"))
	    {
			    newPayment.setFieldValue("REFDOC", ccAuthField.getFieldValue());
				refDoc = ccAuthField.getFieldValue();
	    }

	    if (rbPerCheqField.getFieldValue().equals("X"))
	    {
			    newPayment.setFieldValue("REFDOC", ChkNoField.getFieldValue());
				refDoc = ChkNoField.getFieldValue();
	    }

	    if (rbBankCheqField.getFieldValue().equals("X"))
	    {
			    newPayment.setFieldValue("REFDOC", ChkNoField.getFieldValue());
				refDoc = ChkNoField.getFieldValue();
		}

	    
		
		newPayment.setFieldValue("VTI_REF", orderNo);
		newPayment.setFieldValue("SERVERID", vtiServerId);
		newPayment.setIntegerFieldValue("PAYMENT_SEQ", paymentSequence);
		newPayment.setDoubleFieldValue("AMOUNT", payAmount);
		newPayment.setFieldValue("PAYMENT_TYPE", payType);
		newPayment.setFieldValue("PAY_DATE", currDate);
		newPayment.setFieldValue("PAY_TIME", currTime);
		newPayment.setFieldValue("TIMESTAMP", "INPROGRESS");
		newPayment.setFieldValue("VOUCHER_NO", voucherNo);
		newPayment.setIntegerFieldValue("POS_LANE", deviceId);
		newPayment.setFieldValue("CC_TYPE", ccType);
//		newPayment.setFieldValue("CC_AUTH", ////ccAuth);
		newPayment.setFieldValue("CHKNO", ChkNoField.getFieldValue());
	    newPayment.setFieldValue("DRAWER_NAME", accNam.getFieldValue());
	    newPayment.setFieldValue("ACCOUNT_NO", accNo.getFieldValue());
	    newPayment.setFieldValue("BRANCH", branch.getFieldValue());
	    newPayment.setFieldValue("TELNO", telNum.getFieldValue());
		newPayment.setFieldValue("PURCHORDER", sessionHeader.getUserId());
	    
		try
		{
			paymentLdbTable.saveRow(newPayment);
		}
		catch (VtiExitException kk)
		{
			Log.error("Error Saving New Payment", kk);
			return new VtiUserExitResult(999, "Error Saving New Payment");
		}

		// Update the Screen Fields
		String blank = " ";

		totalOwingField.setDoubleFieldValue(totalOwing);
		totalPaidField.setDoubleFieldValue(totalPaid);
		payAmountField.setFieldValue(blank);
		totalChangeField.setFieldValue(totalChange);

		ccTypeField.setFieldValue(ccType);
		////ccAuthField.setFieldValue(////ccAuth);


		// Update the Screen Table

		String stPaySeq = Integer.toString(paymentSequence);
		String stPayAmount = StringUtil.doubleToString(payAmount, 2);

		VtiUserExitScreenTableRow newScreenRow = paymentHistory.getNewRow();

//		newScreenRow.setFieldValue("VTI_REF", orderNo);
//		newScreenRow.setFieldValue("PAYMENT_SEQ", stPaySeq);
//		newScreenRow.setFieldValue("PAYMENT_TYPE", payType);
//		newScreenRow.setFieldValue("AMOUNT", stPayAmount);
//		newScreenRow.setFieldValue("PAY_DATE", currDate);
//		newScreenRow.setFieldValue("PAY_TIME", currTime);
//		newScreenRow.setFieldValue("L_CC_AUTH", ////ccAuth);
//		newScreenRow.setFieldValue("L_CC_TYPE", ccType);
		
	    newScreenRow.setFieldValue("VTI_REF", orderNo);
	    newScreenRow.setFieldValue("PAYMENT_SEQ", stPaySeq);
	    newScreenRow.setFieldValue("PAYMENT_TYPE", payType);
	    newScreenRow.setFieldValue("AMOUNT", stPayAmount);
	    newScreenRow.setFieldValue("REFDOC", refDoc);
	    newScreenRow.setFieldValue("PAY_DATE", currDate);
	    newScreenRow.setFieldValue("PAY_TIME", currTime);
//	    newScreenRow.setFieldValue("L_CC_AUTH", ////////ccAuth);
		paymentHistory.appendRow(newScreenRow);

	//POSPOLE Print
	StringBuffer posMes = new StringBuffer();;
	posMes.append(makePOSLine("Due","", totalOwing));
	VtiExitKeyValuePair[] keyValuePairs = 
		{
			new VtiExitKeyValuePair("&Line1&", posMes.toString()),
		};
	VtiExitKeyValuePair[] keyOpen = 
		{
		};
	try
		{
				invokePrintTemplate("PoleReset", keyOpen);
				invokePrintTemplate("PoleMessage", keyValuePairs);
		}
	catch (VtiExitException ee)
		{
		}
	 //POSPOLE end
		///////////////////////////////////
		//print small docket
		///////////////////////////////////

		PrintSmallDocket printObject = new PrintSmallDocket();
		Hashtable paymentDetails = new Hashtable();

		paymentDetails.put("PAY_AMOUNT", new Double(payAmount));
		paymentDetails.put("TOTAL_CHANGE", new Double(totalChange));
		paymentDetails.put("TOTAL_OWING", new Double(totalOwing));
		paymentDetails.put("PAYMENT_TYPE", paymentDesc);
		int intDocType = printObject.SALE;
		if (docType.equals("PRE_SALE"))
		{
			intDocType = printObject.PRE_SALE;

		}


		/*

    try
    {
      printObject.print(this, orderNo, intDocType, paymentDetails, deviceId);
    }
    catch (VtiExitException ee)
    {
      return new VtiUserExitResult(999, "Error Printing Docket");
    }

    ///////////////////////////////////
    //Open the Cashdrawer.
    ///////////////////////////////////

    //Cash drawer connection definition, together with setting of IP address;
    if (openCashDrawer)
    {
      String ipAddress = sessionHeader.getRemoteHostAddress();
      try
      {
        POS_Connection cashDrawer =
            new POS_Connection(ipAddress, 5092, new DelimitedInputHandler());
        cashDrawer.open();
        cashDrawer.write("OpenCashDrawer \n\r");
        cashDrawer.close();
      }
      catch (Exception e)
      {
        Log.warn("Error Opening Cash Drawer", e);
      }
    }

		 */

		return new VtiUserExitResult(000, "Payment Processed");
	}
	

  	//PosPole Display amnt is a string
	//Create actual Message
  	private StringBuffer makePOSLine(String title, String desc,  String amnt)
	{
		StringBuffer makeLI = new StringBuffer();
		String lineReturn = System.getProperty("line.separator");
		String pos20 = "";
		if(desc.length() < 20)
			pos20 = desc.substring(0,desc.length());
		else
			pos20 = desc.substring(0,20);
		
		DecimalFormat df1 = new DecimalFormat("######0.00");
		
		makeLI.append(pos20);
		makeLI.append(title + " : ");
		makeLI.append(amnt);

		return makeLI;
	}
	
	//PosPole Display amnt is a double
	//Create actual msssage
  	private StringBuffer makePOSLine(String title, String desc,  double amnt)
	{
		StringBuffer makeLI = new StringBuffer();
		String lineReturn = System.getProperty("line.separator");
		String pos20 = "";
		if(desc.length() < 20)
			pos20 = desc.substring(0,desc.length());
		else
			pos20 = desc.substring(0,20);
		
		DecimalFormat df1 = new DecimalFormat("######0.00");
		
		makeLI.append(pos20);
		makeLI.append(title + " : ");
		makeLI.append(df1.format(amnt));

		return makeLI;
	}
}
