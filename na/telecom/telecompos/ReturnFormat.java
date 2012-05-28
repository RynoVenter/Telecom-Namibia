package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import java.text.*;

// This exit is called from the screen format for the return screen.
// It sets up the current position for the order, i.e. based on order total
// and order paid, what is owing.

public class ReturnFormat
    extends VtiUserExit
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
    VtiUserExitScreenField ordTypField = getScreenField("ORDTYP");	
    VtiUserExitScreenField refOrderField = getScreenField("REF_ORDER");	
	VtiUserExitScreenField accNo = getScreenField("ACC_NUM");
   
	if (accNo == null) return new VtiUserExitResult(999, "Field ACC_NUM not found");
    if (orderTotalField == null) return new VtiUserExitResult(999, "Field ORDER_TOTAL not found");
    if (totalPaidField == null) return new VtiUserExitResult(999, "Field TOTAL_PAID not found");
    if (totalOwingField == null) return new VtiUserExitResult(999, "Field TOTAL_OWING not found");
    if (totalOwingCstField == null) return new VtiUserExitResult(999, "Field TOTAL_OWING_CST not found");
    if (orderNoField == null) return new VtiUserExitResult(999, "Field VTI_REF not found");
    if (returnNoField == null) return new VtiUserExitResult(999, "Field RETURN_NO not found");
    if (totalChangeField == null) return new VtiUserExitResult(999, "Field TOTAL_CHANGE not found");
    if (payAmountField == null)  return new VtiUserExitResult(999, "Field PAY_AMOUNT not found");
    
    
    VtiExitLdbTable docHeaderLdbTable = getLocalDatabaseTable("YSPS_DOC_HEADER");
    VtiExitLdbTable documentItemLdbTable = getLocalDatabaseTable("YSPS_DOC_ITEMS");

    if (docHeaderLdbTable == null)
      return new VtiUserExitResult (999, "LDB table YSPS_DOC_HEADER not found");
    if (documentItemLdbTable == null)
      return new VtiUserExitResult (999, "LDB table YSPS_DOC_ITEMS not found");


    double totalOwing = 0;
    double orderTotal = 0;
    double totalPaid = 0;
    String vtiRef = orderNoField.getFieldValue();
    String vtiServerId = getVtiServerId();
    String ordType = ordTypField.getFieldValue();
    String retOrdTyp = ordType + "-RET";
    
	ordTypField.setFieldValue(retOrdTyp);
	
	String refOrder = "??-" + vtiServerId;
    refOrderField.setFieldValue(refOrder);
	accNo.setDisplayOnlyFlag(true);
    // Select the doc header record in order to get the latest payment
    // situation.
    VtiExitLdbSelectCriterion[] headerSelConds =
        {
        new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiRef),
        new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
    };

    VtiExitLdbSelectConditionGroup headerSelCondGrp = new VtiExitLdbSelectConditionGroup(headerSelConds, true);

    // Fetch the corresponding items from the YSPS_DOC_HEADER
    // LDB table.
    VtiExitLdbTableRow[] docHeaderLdbRows = docHeaderLdbTable.getMatchingRows(headerSelCondGrp);

    // Check that the order was found.
    if (docHeaderLdbRows.length == 0)
      return new VtiUserExitResult(999, "Order Not Found");


    // There should be only a single row returned.  As such we will work with it only.
    orderTotal = docHeaderLdbRows[0].getDoubleFieldValue("ORDER_TOTAL");
    totalPaid = docHeaderLdbRows[0].getDoubleFieldValue("TOTAL_PAID");
    String docType = docHeaderLdbRows[0].getFieldValue("DOC_TYPE");

    // Calculate the total amount owing on this order
    totalOwing = orderTotal - totalPaid;
	//Format the totalOwing attribute to the nearest 5 cent downward so it maybe ready for payment as soon as
	//the teller goes into the payment screen
	
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
