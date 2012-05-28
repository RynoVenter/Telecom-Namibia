package na.telecom.telecompos;

import java.util.Date;
import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;
import au.com.skytechnologies.ecssdk.util.DateFormatter;

import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitLdbSelectCondition;
import au.com.skytechnologies.vti.VtiExitLdbSelectConditionGroup;
import au.com.skytechnologies.vti.VtiExitLdbSelectCriterion;
import au.com.skytechnologies.vti.VtiExitLdbTable;
import au.com.skytechnologies.vti.VtiExitLdbTableRow;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;

// This exit is called from the screen format for the payment screen.
// It sets up the current position for the order, i.e. based on order total
// and order paid, what is owing.

public class ICMSPaymentFormat extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
    // Get Screen Fields we will need and check that they exist.

    VtiUserExitScreenField orderTotalField = getScreenField("ORDER_TOTAL");
    VtiUserExitScreenField totalPaidField = getScreenField("TOTAL_PAID");
    VtiUserExitScreenField totalOwingField = getScreenField("TOTAL_OWING");
    VtiUserExitScreenField totalOwingCstField = getScreenField("TOTAL_OWING_CST");
    VtiUserExitScreenField orderNoField = getScreenField("ORDER_NO");
    VtiUserExitScreenField returnNoField = getScreenField("RETURN_NO");
    VtiUserExitScreenField totalChangeField = getScreenField("TOTAL_CHANGE");
    VtiUserExitScreenField payAmountField = getScreenField("PAY_AMOUNT");
    VtiUserExitScreenField payTypeField = getScreenField("PAY_TYPE");
    
    VtiUserExitScreenField rCashField = getScreenField("R_CASH");
    VtiUserExitScreenField rEFTDebitField = getScreenField("R_EFT_DEBIT");
    VtiUserExitScreenField rEFTCreditField = getScreenField("R_EFT_CREDIT");
    VtiUserExitScreenField rEFTPOSField = getScreenField("R_EFTPOS");
    VtiUserExitScreenField rPerChequeField = getScreenField("R_PER_CHEQUE");
    VtiUserExitScreenField rBankChequeField = getScreenField("R_BANK_CHEQUE");
    VtiUserExitScreenField creditCardField = getScreenField("CREDITCARD");
    VtiUserExitScreenField chequeNoField = getScreenField("CHEQUE_NO");
    VtiUserExitScreenField accNameField = getScreenField("ACC_NAME");
    VtiUserExitScreenField accNoField = getScreenField("ACC_NUM");
    VtiUserExitScreenField branchField = getScreenField("BRANCH");
    VtiUserExitScreenField telNoField = getScreenField("TEL_NUM");
    VtiUserExitScreenField giftvoucherField = getScreenField("R_GIFT_VOUCH");
    VtiUserExitScreenField voucherNoField = getScreenField("VOUCHER_NO");

    if (orderTotalField == null) return new VtiUserExitResult(999, "Field ORDER_TOTAL not found");
    if (totalPaidField == null) return new VtiUserExitResult(999, "Field TOTAL_PAID not found");
    if (totalOwingField == null) return new VtiUserExitResult(999, "Field TOTAL_OWING not found");
    if (totalOwingCstField == null) return new VtiUserExitResult(999, "Field TOTAL_OWING_CST not found");
    if (orderNoField == null) return new VtiUserExitResult(999, "Field VTI_REF not found");
    if (returnNoField == null) return new VtiUserExitResult(999, "Field RETURN_NO not found");
    if (totalChangeField == null) return new VtiUserExitResult(999, "Field TOTAL_CHANGE not found");
    if (payAmountField == null) return new VtiUserExitResult(999, "Field PAY_AMOUNT not found");
    if (payTypeField == null) return new VtiUserExitResult(999, "Field PAY_TYPE not found");
    if (rCashField == null) return new VtiUserExitResult(999, "Field R_CASH not found");
    if (rEFTCreditField == null) return new VtiUserExitResult(999, "Field R_EFT_CREDIT not found");
    if (rEFTDebitField == null) return new VtiUserExitResult(999, "Field R_EFT_DEBIT not found");
    if (rEFTPOSField == null) return new VtiUserExitResult(999, "Field R_EFTPOS not found");
    if (rPerChequeField == null) return new VtiUserExitResult(999, "Field R_PER_CHEQUE not found");
    if (rBankChequeField == null) return new VtiUserExitResult(999, "Field R_BANK_CHEQUE not found");
    if (creditCardField == null) return new VtiUserExitResult(999, "Field CREDITCARD not found");
    if (chequeNoField == null) return new VtiUserExitResult(999, "Field CHEQUE_NO not found");
    if (accNameField == null) return new VtiUserExitResult(999, "Field ACC_NAME not found");
    if (accNoField == null) return new VtiUserExitResult(999, "Field ACC_NO not found");
    if (branchField == null) return new VtiUserExitResult(999, "Field BRANCH not found");
    if (telNoField == null) return new VtiUserExitResult(999, "Field TEL_NO not found");
    
    VtiExitLdbTable docHeaderLdbTable = getLocalDatabaseTable("YSPS_ICMS_INV");

    if (docHeaderLdbTable == null) return new VtiUserExitResult (999, "LDB table YSPS_ICMS_INV not found");

    double totalOwing = 0;
    double orderTotal = 0;
    double totalPaid = 0;
    String vtiRef = orderNoField.getFieldValue();
    String vtiServerId = getVtiServerId();
    String strPayType = payTypeField.getFieldValue();
    
    // Select the doc header record in order to get the latest payment
    // situation.
    VtiExitLdbSelectCriterion[] headerSelConds =
        {
        new VtiExitLdbSelectCondition("REFERENCE_NO", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiRef),
        new VtiExitLdbSelectCondition("SERVER_ID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
    };

    VtiExitLdbSelectConditionGroup headerSelCondGrp = new VtiExitLdbSelectConditionGroup(headerSelConds, true);

    // Fetch the corresponding items from the YSPS_DOC_HEADER
    // LDB table.
    VtiExitLdbTableRow[] docHeaderLdbRows = docHeaderLdbTable.getMatchingRows(headerSelCondGrp);

    // Check that the order was found.
    if (docHeaderLdbRows.length == 0) return new VtiUserExitResult(999, "Order Not Found");


    for(int i=0; i<docHeaderLdbRows.length; i++)
    {
    	orderTotal+=docHeaderLdbRows[i].getDoubleFieldValue("PAYMENT_AMT");
    }
        //Determine pay type
    if(strPayType.equals("CASH"))
    {
    	rCashField.setDisplayOnlyFlag(false);
        rEFTDebitField.setDisplayOnlyFlag(true);
        rEFTCreditField.setDisplayOnlyFlag(true);
        rEFTPOSField.setDisplayOnlyFlag(true);
        rPerChequeField.setDisplayOnlyFlag(true);
        rBankChequeField.setDisplayOnlyFlag(true);
        creditCardField.setDisplayOnlyFlag(true);
        chequeNoField.setDisplayOnlyFlag(true);
        accNameField.setDisplayOnlyFlag(true);
        accNoField.setDisplayOnlyFlag(true);
        branchField.setDisplayOnlyFlag(true);
        telNoField.setDisplayOnlyFlag(true);
        giftvoucherField.setDisplayOnlyFlag(true);
        voucherNoField.setDisplayOnlyFlag(true);
        rCashField.setFieldValue("X");
    }
    else if(strPayType.equals("CREDIT CARD"))
    {
       	rCashField.setDisplayOnlyFlag(true);
        rEFTDebitField.setDisplayOnlyFlag(false);
        rEFTCreditField.setDisplayOnlyFlag(false);
        rEFTPOSField.setDisplayOnlyFlag(false);
        rPerChequeField.setDisplayOnlyFlag(true);
        rBankChequeField.setDisplayOnlyFlag(true);
        creditCardField.setDisplayOnlyFlag(false);
        chequeNoField.setDisplayOnlyFlag(true);
        accNameField.setDisplayOnlyFlag(true);
        accNoField.setDisplayOnlyFlag(true);
        branchField.setDisplayOnlyFlag(true);
        telNoField.setDisplayOnlyFlag(true);
        giftvoucherField.setDisplayOnlyFlag(true);
        voucherNoField.setDisplayOnlyFlag(true);

        rEFTCreditField.setFieldValue("X");
        rCashField.setFieldValue("");
    }
    else //CHEQUE
    {
       	rCashField.setDisplayOnlyFlag(true);
        rEFTDebitField.setDisplayOnlyFlag(true);
        rEFTCreditField.setDisplayOnlyFlag(true);
        rEFTPOSField.setDisplayOnlyFlag(true);
        rPerChequeField.setDisplayOnlyFlag(false);
        rBankChequeField.setDisplayOnlyFlag(false);
        creditCardField.setDisplayOnlyFlag(true);
        accNameField.setDisplayOnlyFlag(false);
        accNoField.setDisplayOnlyFlag(false);
        branchField.setDisplayOnlyFlag(false);
        telNoField.setDisplayOnlyFlag(false);
        chequeNoField.setDisplayOnlyFlag(false);
        giftvoucherField.setDisplayOnlyFlag(true);
        voucherNoField.setDisplayOnlyFlag(true);

        rPerChequeField.setFieldValue("X");
        rCashField.setFieldValue("");
        
    }	
	
    // Calculate the total amount owing on this order
    totalOwing = orderTotal - totalPaid;

	//Format the totalOwing attribute to the nearest 5 cent downward so it maybe ready for payment as soon as
	//the teller goes into the payment screen
	if(rCashField.getFieldValue().equals("X"))//Added during Go Live 05/06
		totalOwing = totalOwing - roundCent(totalOwing);
	
    // Set the values onto the screen.
    orderTotalField.setDoubleFieldValue(totalOwing);
    totalPaidField.setDoubleFieldValue(totalPaid);
    totalOwingField.setDoubleFieldValue(totalOwing);
    totalOwingCstField.setDoubleFieldValue(totalOwing);
    payAmountField.setDoubleFieldValue(totalOwing);
	
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

    return new VtiUserExitResult();
  }
  
  public double roundCent(double tOwing) throws VtiExitException
  {
	  
		double formTotOwe = tOwing % 0.05;
		
		if(formTotOwe > 0.04)
			formTotOwe = 0;
  
  return formTotOwe;
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
