package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class SearchAddItem extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
    // Data Declarations.  
    VtiUserExitScreenField orderNoField = getScreenField("ORDER_NO");
    VtiUserExitScreenField materialField = getScreenField("MATERIAL");
    VtiUserExitScreenField currDescField = getScreenField("CURR_MAT_DESC");
    VtiUserExitScreenField currQtyField = getScreenField("CURR_QTY");
    VtiUserExitScreenField currValueField = getScreenField("CURR_VALUE");
    VtiUserExitScreenField currItemNoField = getScreenField("CURR_ITEM_NO");
	VtiUserExitScreenField printOrdNo = getScreenField("PRINT_ORDNO");
	VtiUserExitScreenField ordTyp = getScreenField("ORDTYP");
	VtiUserExitScreenField cust = getScreenField("KUNNR");
	VtiUserExitScreenField ordTypCd = getScreenField("ORD_TP_CD");
	
	
	if (ordTyp == null) return new VtiUserExitResult(999, "Screen Field ORDTYP does not exist");
    if (orderNoField == null) return new VtiUserExitResult(999, "Screen Field ORDER_NO does not exist");    
    if (materialField == null) return new VtiUserExitResult(999, "Screen Field MATERIAL does not exist");
    if (currDescField == null) return new VtiUserExitResult(999, "Screen Field CURR_MAT_DESC does not exist");
    if (currQtyField == null) return new VtiUserExitResult(999, "Screen Field CURR_QTY does not exist");
    if (currValueField == null) return new VtiUserExitResult(999, "Screen Field CURR_VALUE does not exist");
    if (currItemNoField == null) return new VtiUserExitResult(999, "Screen Field CURR_ITEM_NO does not exist");  
	if (printOrdNo == null)  return new VtiUserExitResult(999, "Screen Field PRINT_ORDNO does not exist");
	if (cust == null) return new VtiUserExitResult(999, "Screen Field KUNNR does not exist");
	if (ordTypCd == null) return new VtiUserExitResult(999, "Screen Field ORD_TP_CD does not exist");
    
    VtiExitLdbTable docHeaderLdbTable = getLocalDatabaseTable("YSPS_DOC_HEADER");
    VtiExitLdbTable docItemsLdbTable = getLocalDatabaseTable("YSPS_DOC_ITEMS");
    VtiExitLdbTable materialLdbTable = getLocalDatabaseTable("YSPS_MATERIAL");
    VtiExitLdbTable discountLdbTable = getLocalDatabaseTable("YSPS_DISCOUNT");
	
    if (docHeaderLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_DOC_HEADER not found");
    if (docItemsLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_DOC_ITEMS not found");
    if (materialLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_MATERIAL not found");
    if (discountLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_DISCOUNT not found");

    String vtiServerId = getVtiServerId();
    String orderNo = orderNoField.getFieldValue();
    String material = materialField.getFieldValue();
    String itemNo = "";
    double tax = 0.10;
    double orderTotal = 0;
    double orderTax = 0;
	String printOrdNum = "";
    int hasTax = 0;
	double disc = 0;
	double discCalc =0;
	double discVal = 0;
    // If no Item Number "scanned" we can exit.
    if (material.equals(""))
        return new VtiUserExitResult(999, "No Material Selected");
	
	String sCon = ordTyp.getFieldValue();
	if(sCon.equalsIgnoreCase("CASH SALE"))
	{
		hasTax = 1;
	}
	if(sCon.equalsIgnoreCase("STAFF SALE"))
	{
		hasTax = 1;
	}
	if(sCon.equalsIgnoreCase("TELLY WALKER"))
	{
		hasTax = 1;
	}
	if(sCon.equalsIgnoreCase("TELLY POINT"))
	{
		hasTax = 1;
	}
	if(sCon.equalsIgnoreCase("ICMS"))
	{
		hasTax = 1;
	}
	if(sCon.equalsIgnoreCase("TAX EXEMPT"))
	{
		hasTax = 0;
	}
    
    // Get a New Order Number.
    if (orderNo.equals(""))
    {
        long order = 0;
        try
        {
            order = getNextNumberFromNumberRange("YSPS_ORDER");
        }
        catch (VtiExitException ee)
        {
            return new VtiUserExitResult(999, "Failed to Get Order Number");
        }
        
        orderNo = Long.toString(order);
        orderNoField.setFieldValue(orderNo);
    }
    
    // Select the LDB to confirm the item has not already been added.
    VtiExitLdbSelectCriterion[] itemSelConds =
    {
        new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
        new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId),
        new VtiExitLdbSelectCondition("MATERIAL", VtiExitLdbSelectCondition.EQ_OPERATOR, material)
    };
                
    VtiExitLdbSelectConditionGroup itemSelCondGrp = new VtiExitLdbSelectConditionGroup(itemSelConds, true);
  
    VtiExitLdbTableRow[] itemLdbRows = docItemsLdbTable.getMatchingRows(itemSelCondGrp);
            
    // If the item exists, then exit...no more to do.
    if (itemLdbRows.length != 0)
        return new VtiUserExitResult();
    

    // Select the document header record.
    VtiExitLdbSelectCriterion[] headerSelConds =
    {
    new VtiExitLdbSelectCondition("VTI_REF",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
    new VtiExitLdbSelectCondition("SERVERID",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
    };
        
    VtiExitLdbSelectConditionGroup headerSelCondGrp =
        new VtiExitLdbSelectConditionGroup(headerSelConds, true);
  
    VtiExitLdbTableRow[] headerLdbRows =
        docHeaderLdbTable.getMatchingRows(headerSelCondGrp);

    VtiExitLdbTableRow headerLdbRow = null;
    
    // If no record returned, create the new Row and update the basics.
    if (headerLdbRows.length == 0)
    {
        headerLdbRow = docHeaderLdbTable.newRow();
        headerLdbRow.setFieldValue("DOC_TYPE", "ORDER");
        headerLdbRow.setFieldValue("VTI_REF", orderNo);
        headerLdbRow.setFieldValue("SERVERID", vtiServerId);
        headerLdbRow.setFieldValue("TIMESTAMP", "INPROGRESS");
		headerLdbRow.setFieldValue("ORDER_NO",printOrdNo.getFieldValue());
    }
    else
        headerLdbRow = headerLdbRows[0];

    // Try to select the  Material.  
    
    VtiExitLdbSelectCriterion materialSelConds = 
      new VtiExitLdbSelectCondition("MATERIAL", VtiExitLdbSelectCondition.EQ_OPERATOR, material);
    
    VtiExitLdbTableRow[] materialLdbRows = 
        materialLdbTable.getMatchingRows(materialSelConds);    

    if (materialLdbRows.length == 0)
        return new VtiUserExitResult(999, "Material Not Found in DB");    
    
    // Now Create a New LDB Item to add to the order    
    
    long longItemNo = this.getNextNumberFromNumberRange("YSPS_ITEM");
    itemNo = Long.toString(longItemNo);
        
    VtiExitLdbTableRow newItemRow = docItemsLdbTable.newRow();
        
    newItemRow.setFieldValue("VTI_REF", orderNo); 
    newItemRow.setFieldValue("SERVERID", vtiServerId);
    newItemRow.setFieldValue("ITEM_NO", itemNo);
    newItemRow.setFieldValue("MATERIAL", material);
    newItemRow.setFieldValue("MAT_DESC", materialLdbRows[0].getFieldValue("MAT_DESC"));
    newItemRow.setFieldValue("EAN", materialLdbRows[0].getFieldValue("EAN"));
	newItemRow.setFieldValue("ORDER_NO",printOrdNo.getFieldValue());
        
    double rrPrice = materialLdbRows[0].getDoubleFieldValue("RR_PRICE");
        
    newItemRow.setDoubleFieldValue("RR_PRICE", rrPrice);
    newItemRow.setDoubleFieldValue("ACT_PRICE", rrPrice);
    newItemRow.setFieldValue("ITEM_QTY", "1");
                
    double taxPrice = rrPrice * (1 + tax * hasTax);
    newItemRow.setDoubleFieldValue("RRP_INCLTAX", taxPrice);
    newItemRow.setDoubleFieldValue("ACTPRICE_INCTAX", taxPrice);
                   
        
          
//discount		
						VtiExitLdbSelectCriterion[] discountSelConds =
				{
					new VtiExitLdbSelectCondition("KSCHL",VtiExitLdbSelectCondition.EQ_OPERATOR,ordTypCd.getFieldValue()),
					new VtiExitLdbSelectCondition("KSTBM",VtiExitLdbSelectCondition.LE_OPERATOR,"1"),
					new VtiExitLdbSelectCondition("MATNR",VtiExitLdbSelectCondition.EQ_OPERATOR,material),
					new VtiExitLdbSelectCondition("KUNNR",VtiExitLdbSelectCondition.EQ_OPERATOR,cust.getFieldValue()),
				};

				VtiExitLdbSelectConditionGroup discountSelCondsGrp = new VtiExitLdbSelectConditionGroup(discountSelConds, true);
  				VtiExitLdbTableRow [] discountLdbRows = discountLdbTable.getMatchingRows(discountSelCondsGrp);	

				if(discountLdbRows.length>0)
				{
					for(int nextDiscCond = 0 ;nextDiscCond < discountLdbRows.length; nextDiscCond++)
						{
							if(1 == discountLdbRows[nextDiscCond].getLongFieldValue("KSTBM"))
								disc = discountLdbRows[nextDiscCond].getDoubleFieldValue("KBETR")* -1 / 10;
						}
				}
				
				if (disc > 0)
				{
					discCalc =rrPrice;
					discVal = discCalc * disc / 100;
					discCalc = discCalc - discVal;
				}
				
//discount	
	newItemRow.setDoubleFieldValue("ITEM_TOTAL", taxPrice); 	
    try
    {
        docItemsLdbTable.saveRow(newItemRow);
    }
    catch(VtiExitException ee)
    {
        Log.error("Error Saving New Item LDB Row", ee);
        return new VtiUserExitResult(999, "Error Updating New Item LDB Row");
    }  
    
    // Add the value of the newly added item to the order total.              
        
    orderTotal = headerLdbRow.getDoubleFieldValue("ORDER_TOTAL") + taxPrice;
    orderTax = orderTotal * (tax * hasTax);              
      
    headerLdbRow.setDoubleFieldValue("ORDER_TOTAL", orderTotal);
    headerLdbRow.setDoubleFieldValue("ORDER_GST", orderTax);
    headerLdbRow.setFieldValue("TIMESTAMP", "INPROGRESS");
    
    try
    {
        docHeaderLdbTable.saveRow(headerLdbRow);
    }
    catch (VtiExitException ee)
    {
        Log.error("Error Saving Doc Header Row", ee);
        return new VtiUserExitResult
            (999, "Error Saving the Order");
    }    
    
    // Update the screen fields of current item.
    currDescField.setFieldValue(materialLdbRows[0].getFieldValue("MAT_DESC"));
    currQtyField.setIntegerFieldValue(1);
    currValueField.setDoubleFieldValue(taxPrice);
    currItemNoField.setFieldValue(itemNo);

    return new VtiUserExitResult();
  }
}
