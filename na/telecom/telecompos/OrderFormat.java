package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class OrderFormat extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
	  
	VtiUserExitScreenField fireUp = getScreenField("G_PRCHD");
	if (fireUp == null) return new VtiUserExitResult(999, "Screen Field G_PRCHD does not exist");
	VtiUserExitScreenField test = getScreenField("TEST");
	VtiUserExitScreenTable itemsScreenTable = getScreenTable("ITEMS");

    if (itemsScreenTable == null) return new VtiUserExitResult(999, "Screen table ITEMS not found");

	VtiUserExitScreenField ordTyp = getScreenField("ORDTYP");
	VtiUserExitScreenField cust = getScreenField("KUNNR");
	VtiUserExitScreenField serialNo = getScreenField("SERIAL_NO");
	VtiUserExitScreenField costCent = getScreenField("KOSTL");
	VtiUserExitScreenField tellTyp = getScreenField("TELLTYPE");
	VtiUserExitScreenField name1 = getScreenField("NAME1");
	VtiUserExitScreenField item = getScreenField("ITEM");
	VtiUserExitScreenField staff = getScreenField("G_STAFF");	
	VtiUserExitScreenField empNo = getScreenField("EMP_NO");	
	VtiUserExitScreenField curMatDesc = getScreenField("CURR_MAT_DESC");
	VtiUserExitScreenField curQty = getScreenField("CURR_QTY");
	VtiUserExitScreenField curVal = getScreenField("CURR_VALUE");
	VtiUserExitScreenField ordTax = getScreenField("ORDER_TAX");
	VtiUserExitScreenField ordVal = getScreenField("ORDER_VALUE");
	VtiUserExitScreenField orderNoField = getScreenField("ORDER_NO");
    VtiUserExitScreenField orderTotalField = getScreenField("ORDER_VALUE");
    VtiUserExitScreenField orderTaxField = getScreenField("ORDER_TAX");
    VtiUserExitScreenField scanItemField = getScreenField("ITEM");
    VtiUserExitScreenField currDescField = getScreenField("CURR_MAT_DESC");
    VtiUserExitScreenField currQtyField = getScreenField("CURR_QTY");
    VtiUserExitScreenField currValueField = getScreenField("CURR_VALUE");
    VtiUserExitScreenField currItemNoField = getScreenField("CURR_ITEM_NO");
	VtiUserExitScreenField ordTypCd = getScreenField("ORD_TP_CD");
	VtiUserExitScreenField gVat = getScreenField("G_VAT");
	VtiUserExitScreenField discT = getScreenField("DISCOUNT");
	VtiUserExitScreenField currMatNo = getScreenField("CURR_MATNR");
	VtiUserExitScreenField prChd = getScreenField("G_PRCHD");
	VtiUserExitScreenField wrkFEanScan = getScreenField("CURRSCANITEM");
	
	if (ordTyp == null) return new VtiUserExitResult(999, "Screen Field ORDTYP does not exist");
	if (cust == null) return new VtiUserExitResult(999, "Screen Field KUNNR does not exist");
	if (staff == null) return new VtiUserExitResult(999, "Screen Field G_STAFF does not exist");	
	if (serialNo == null) return new VtiUserExitResult(999, "Screen Field SERIAL_NO does not exist");
	if (empNo == null) return new VtiUserExitResult(999, "Screen Field EMP_NO does not exist");	
	if (costCent == null) return new VtiUserExitResult(999, "Screen Field KOSTL does not exist");
	if (tellTyp == null) return new VtiUserExitResult(999, "Screen Field TELLTYPE does not exist");
	if (name1 == null) return new VtiUserExitResult(999, "Screen Field NAME1 does not exist");
	if (item == null) return new VtiUserExitResult(999, "Screen Field ITEM does not exist");
	if (curMatDesc == null) return new VtiUserExitResult(999, "Screen Field CURR_MAT_DESC does not exist");
	if (curQty == null) return new VtiUserExitResult(999, "Screen Field CURR_QTY does not exist");
	if (curVal == null) return new VtiUserExitResult(999, "Screen Field CURR_VALUE does not exist");
	if (ordTax == null) return new VtiUserExitResult(999, "Screen Field ORDER_TAX does not exist");
	if (ordVal == null) return new VtiUserExitResult(999, "Screen Field ORDER_VALUE does not exist");
	if (test == null) return new VtiUserExitResult(999, "Screen Field t does not exist");
	if (currMatNo == null)return new VtiUserExitResult(999, "Screen Field CURR_MATNR does not exist");
	if (orderNoField == null)return new VtiUserExitResult(999, "Screen Field ORDER_NO does not exist");
    if (orderTotalField == null)return new VtiUserExitResult(999, "Screen Field ORDER_VALUE does not exist");
    if (orderTaxField == null)return new VtiUserExitResult(999, "Screen Field ORDER_TAX does not exist");
    if (scanItemField == null)return new VtiUserExitResult(999, "Screen Field ITEM does not exist");
    if (currDescField == null) return new VtiUserExitResult(999, "Screen Field CURR_MAT_DESC does not exist");
    if (currQtyField == null) return new VtiUserExitResult(999, "Screen Field CURR_QTY does not exist");
    if (currValueField == null) return new VtiUserExitResult(999, "Screen Field CURR_VALUE does not exist");
    if (currItemNoField == null)return new VtiUserExitResult(999, "Screen Field CURR_ITEM_NO does not exist");
     if (ordTyp == null)return new VtiUserExitResult(999, "Screen Field ORDTYP does not exist");
    if (ordTypCd == null)return new VtiUserExitResult(999, "Screen Field ORD_TP_CD does not exist");
	if (serialNo == null)return new VtiUserExitResult(999, "Screen Field SERIAL_NO does not exist");
	if (cust == null)return new VtiUserExitResult(999, "Screen Field customer does not exist");
	if (prChd == null)return new VtiUserExitResult(999, "Screen Field G_PRCHD does not exist");
	if (wrkFEanScan == null)return new VtiUserExitResult(999, "Screen Field CURRSCANITEM does not exist");
	
	String sCon = ordTyp.getFieldValue();
	boolean clearOrder = false;
	tellTyp.setFieldValue(sCon);



	
	if(fireUp.getFieldValue().equals("X"))
	{
    // Data Declarations.  
    VtiExitLdbTable docHeaderLdbTable = getLocalDatabaseTable("YSPS_DOC_HEADER");
    VtiExitLdbTable docItemsLdbTable = getLocalDatabaseTable("YSPS_DOC_ITEMS");
    VtiExitLdbTable materialLdbTable = getLocalDatabaseTable("YSPS_MATERIAL");
    VtiExitLdbTable discountLdbTable = getLocalDatabaseTable("YSPS_DISCOUNT");

		
    if (docHeaderLdbTable == null)return new VtiUserExitResult(999, "LDB table YSPS_DOC_HEADER not found");
    if (docItemsLdbTable == null)return new VtiUserExitResult(999, "LDB table YSPS_DOC_ITEMS not found");
    if (materialLdbTable == null)return new VtiUserExitResult(999, "LDB table YSPS_MATERIAL not found");
	if (discountLdbTable == null)return new VtiUserExitResult(999, "LDB table YSPS_DISCOUNT not found");

    String vtiServerId = getVtiServerId();
    String orderNo = orderNoField.getFieldValue();
    String scanItem = scanItemField.getFieldValue();
    String material = "";
    String itemNo = Long.toString(currItemNoField.getLongFieldValue());
    double tax = gVat.getDoubleFieldValue();
    double orderTotal = 0;
    double orderTax = 0;
    int newQty = currQtyField.getIntegerFieldValue();
	double hasTax = 1;
	double disc = 0;
	double discCalc = 0;
	double discVal = 0;
	double totalDisc = 0;
	double rowDisc = 0;
	String serialCq = "";
	double screenPrice = currValueField.getDoubleFieldValue();
	boolean adAsNewLine = false;
	boolean useMat = false;
	boolean useOld = true;
	
	
	if(wrkFEanScan.getFieldValue().length() == 0)
		return new VtiUserExitResult(999,"Re-scan the Item please, then update the qty.");
	if(prChd.getFieldValue().equals("X"))
		useOld = false;
	
	String materialNum = "";
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
        return new VtiUserExitResult(999, "Failed to Get Order Number");      
    
    if (newQty == 0)
    {
        
    }

    
    // Select the document header record.
    VtiExitLdbSelectCriterion[] headerSelConds =
    {
    new VtiExitLdbSelectCondition("VTI_REF",VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
    new VtiExitLdbSelectCondition("SERVERID",VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
    };
        
    VtiExitLdbSelectConditionGroup headerSelCondGrp =
        new VtiExitLdbSelectConditionGroup(headerSelConds, true);
  
    VtiExitLdbTableRow[] headerLdbRows =
        docHeaderLdbTable.getMatchingRows(headerSelCondGrp);

    VtiExitLdbTableRow headerLdbRow = null;
    
    // If not record returned, create the new Row and update the basics.
    if (headerLdbRows.length == 0)    
        return new VtiUserExitResult(999, "Failed to select Doc Header");    
    else
        headerLdbRow = headerLdbRows[0];
    
    // If no Item Number "scanned" we can exit.
    if (itemNo.equals(""))    
        return new VtiUserExitResult(999, "No Current Item Selected");                   
   
	
	// Try to select the entered item from the LDB.  First try EAN, then Material.  
    
    VtiExitLdbSelectCriterion eanSelConds = new VtiExitLdbSelectCondition("EAN", VtiExitLdbSelectCondition.EQ_OPERATOR, wrkFEanScan.getFieldValue());
    
    VtiExitLdbTableRow[] materialLdbRows = materialLdbTable.getMatchingRows(eanSelConds);
    
    // If this fails, try the material number.
    if (materialLdbRows.length == 0)
    {
			VtiExitLdbSelectCriterion materialSelConds = new VtiExitLdbSelectCondition("MATERIAL", VtiExitLdbSelectCondition.EQ_OPERATOR, wrkFEanScan.getFieldValue());
        
			materialLdbRows = materialLdbTable.getMatchingRows(materialSelConds);
    }
    
    if (materialLdbRows.length == 0)
        return new VtiUserExitResult(999, "EAN/Material Not Found in DB");
    
    // Get the Material Number.
    material = materialLdbRows[0].getFieldValue("MATERIAL");
	

    // Now Loop through the Screen Table to update the item values.
    
    boolean itemUpdated = false;
    
    for (int i = 0;itemsScreenTable.getRowCount() > i; ++i)
    {   

        VtiUserExitScreenTableRow currentRow = itemsScreenTable.getRow(i);                
        if (currentRow.getFieldValue("ITEM_NO").equals(itemNo))
        {                     
            // Select the LDB and update the QTY.
            VtiExitLdbSelectCriterion[] itemSelConds =
            {
                new VtiExitLdbSelectCondition("VTI_REF",VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
                new VtiExitLdbSelectCondition("SERVERID",VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId),
                new VtiExitLdbSelectCondition("ITEM_NO",VtiExitLdbSelectCondition.EQ_OPERATOR, itemNo)
            };
                
            VtiExitLdbSelectConditionGroup itemSelCondGrp = new VtiExitLdbSelectConditionGroup(itemSelConds, true);
  
            VtiExitLdbTableRow[] itemLdbRows = docItemsLdbTable.getMatchingRows(itemSelCondGrp);
			
            materialNum = itemLdbRows[0].getFieldValue("MATERIAL");
   //Check if this material requires a serial number.
	
	VtiExitLdbSelectCriterion [] serialSelConds = 
			{
				new VtiExitLdbSelectCondition("MATERIAL", VtiExitLdbSelectCondition.EQ_OPERATOR, materialNum),
				new VtiExitLdbSelectCondition("SERIAL_NO_REQD", VtiExitLdbSelectCondition.EQ_OPERATOR, "X")
			};
			
	VtiExitLdbSelectConditionGroup serialSelCondGrp = new VtiExitLdbSelectConditionGroup(serialSelConds, true);
   
    VtiExitLdbTableRow[] serialLdbRows = materialLdbTable.getMatchingRows(serialSelCondGrp);
	
	if(serialLdbRows.length > 0 && useOld == true)
	{
			if(serialLdbRows[0].getFieldValue("SERIAL_NO_REQD").equals("X") && serialNo.getFieldValue().equals(""))
			{
				if(currQtyField.getLongFieldValue()>1)
				{
					setCursorPosition(scanItemField);
					return new VtiUserExitResult(999,"This item may not have 2 quantities, please add as a new item.");
				}
				serialNo.setDisplayOnlyFlag(false);
				currQtyField.setDisplayOnlyFlag(true);
				setCursorPosition(serialNo);
				adAsNewLine = false;
				return new VtiUserExitResult(999,"Please scan the serial bar code.");
			}
			else
			{
			adAsNewLine = true;
			}
	}
 
            if (itemLdbRows.length == 0)
                return new VtiUserExitResult(999, "Error Selecting LDB Item");
            
            // Delete the Item if new Qty is a zero.
            if (newQty == 0)
            {
                // Delete the LDB Item Row.
                try
                {
                    docItemsLdbTable.deleteRow(itemLdbRows[0]);
                }
                catch(VtiExitException ee)
                {
                    Log.error("Error Deleting LDB Row", ee);
                    return new VtiUserExitResult(999, "Error Deleting LDB Item Row");
                }
                
                // Delete the Screen table Row.
                itemsScreenTable.deleteRow(currentRow);
                
                // Clear the "Current Row" Screen Fields.
                currDescField.setFieldValue("");
                currQtyField.setFieldValue("");
                currValueField.setFieldValue("");
                currItemNoField.setFieldValue("");
                itemUpdated = true;
                break;
            }
            else
            {       
                double itemTotal = 0;
				if(useOld)
				{
						itemTotal = itemLdbRows[0].getDoubleFieldValue("ACTPRICE_INCTAX") * newQty;
				}
				else
				{
						if(newQty == 1)
							itemLdbRows[0].setDoubleFieldValue("ACTPRICE_INCTAX",screenPrice);
						itemTotal = screenPrice * newQty;
				}
                itemLdbRows[0].setIntegerFieldValue("ITEM_QTY", newQty);
                itemLdbRows[0].setDoubleFieldValue("ITEM_TOTAL", itemTotal);
                            
                try
                {
                    docItemsLdbTable.saveRow(itemLdbRows[0]);
                }
                catch(VtiExitException ee)
                {
                    Log.error("Error Saving LDB Row", ee);
                    return new VtiUserExitResult(999, "Error Updating LDB Item Row");
                }
            
                // Update the Screen Table.
                currentRow.setLongFieldValue("QTY", newQty);
                currentRow.setDoubleFieldValue("VALUE", itemTotal);
            
//Ryno Venter append
			
				// Select the required records.
				VtiExitLdbSelectCriterion[] discountSelConds =
				{
					new VtiExitLdbSelectCondition("KSCHL",VtiExitLdbSelectCondition.EQ_OPERATOR,ordTypCd.getFieldValue()),
					new VtiExitLdbSelectCondition("KSTBM",VtiExitLdbSelectCondition.LE_OPERATOR,currentRow.getFieldValue("QTY")),
					new VtiExitLdbSelectCondition("MATNR",VtiExitLdbSelectCondition.EQ_OPERATOR,currentRow.getFieldValue("MATERIAL")),
					new VtiExitLdbSelectCondition("KUNNR",VtiExitLdbSelectCondition.EQ_OPERATOR,cust.getFieldValue()),
				};
			
				VtiExitLdbSelectConditionGroup discountSelCondsGrp = new VtiExitLdbSelectConditionGroup(discountSelConds, true);
  				VtiExitLdbTableRow [] discountLdbRows = discountLdbTable.getMatchingRows(discountSelCondsGrp);	
				
				if(discountLdbRows.length>0)
				{
					for(int nextDiscCond = 0 ;nextDiscCond < discountLdbRows.length; nextDiscCond++)
						{
							if(currentRow.getLongFieldValue("QTY") >= discountLdbRows[nextDiscCond].getLongFieldValue("KSTBM"))
								disc = discountLdbRows[nextDiscCond].getDoubleFieldValue("KBETR")* -1 / 10;
						}
				}
				else
				{
					VtiExitLdbSelectCriterion [] discount2SelConds =
						{
						new VtiExitLdbSelectCondition("KSCHL",VtiExitLdbSelectCondition.EQ_OPERATOR,ordTypCd.getFieldValue()),
						new VtiExitLdbSelectCondition("MATNR",VtiExitLdbSelectCondition.EQ_OPERATOR,""),
						new VtiExitLdbSelectCondition("KSTBM",VtiExitLdbSelectCondition.LE_OPERATOR,currentRow.getFieldValue("QTY")),
						new VtiExitLdbSelectCondition("KUNNR",VtiExitLdbSelectCondition.EQ_OPERATOR,cust.getFieldValue()),
						};
			
					VtiExitLdbSelectConditionGroup discount2SelCondsGrp = new VtiExitLdbSelectConditionGroup(discount2SelConds, true);
  					VtiExitLdbTableRow [] discount2LdbRows = discountLdbTable.getMatchingRows(discount2SelCondsGrp);	
				
						if(discount2LdbRows.length>0)
						{
							for(int nextDiscCond = 0 ;nextDiscCond < discount2LdbRows.length; nextDiscCond++)
								{
									if(currentRow.getLongFieldValue("QTY") >= discount2LdbRows[nextDiscCond].getLongFieldValue("KSTBM"))
										disc = discount2LdbRows[nextDiscCond].getDoubleFieldValue("KBETR")* -1 / 10;
								}
						}
						else
						{
							
					   				VtiExitLdbSelectCriterion [] discount3SelConds =
										{
											new VtiExitLdbSelectCondition("KSCHL",VtiExitLdbSelectCondition.EQ_OPERATOR,ordTypCd.getFieldValue()),
											new VtiExitLdbSelectCondition("KSTBM",VtiExitLdbSelectCondition.LE_OPERATOR,currentRow.getFieldValue("QTY")),
											new VtiExitLdbSelectCondition("KUNNR",VtiExitLdbSelectCondition.EQ_OPERATOR,""),
											new VtiExitLdbSelectCondition("MATNR",VtiExitLdbSelectCondition.EQ_OPERATOR,currentRow.getFieldValue("MATERIAL")),
										};
			
										VtiExitLdbSelectConditionGroup discount3SelCondsGrp = new VtiExitLdbSelectConditionGroup(discount3SelConds, true);
  										VtiExitLdbTableRow [] discount3LdbRows = discountLdbTable.getMatchingRows(discount3SelCondsGrp);	
										if(discount3LdbRows.length>0)
										{
												for(int nextDiscCond = 0 ;nextDiscCond < discount3LdbRows.length; nextDiscCond++)
												{
													if(currentRow.getLongFieldValue("QTY") >= discount3LdbRows[nextDiscCond].getLongFieldValue("KSTBM"))
															disc = discount3LdbRows[nextDiscCond].getDoubleFieldValue("KBETR")* -1 / 10;
												}
										}
						}
				}
				
				if (disc > 0)
				{
					discCalc = currentRow.getDoubleFieldValue("VALUE");
					discVal = discCalc * disc / 100;
					discCalc = discCalc - discVal;
					currentRow.setDoubleFieldValue("DISCOUNT_VAL",discVal);
					currentRow.setDoubleFieldValue("VALUE",discCalc);
				}
				
			// Select the LDB and update the QTY.
            VtiExitLdbSelectCriterion[] itemUSelConds =
            {
                new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
                new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId),
                new VtiExitLdbSelectCondition("ITEM_NO", VtiExitLdbSelectCondition.EQ_OPERATOR, itemNo)
            };
                
            VtiExitLdbSelectConditionGroup itemUSelCondGrp = new VtiExitLdbSelectConditionGroup(itemUSelConds, true);
  
            VtiExitLdbTableRow[] itemULdbRows = docItemsLdbTable.getMatchingRows(itemUSelCondGrp);
            
            if (itemLdbRows.length == 0)
                return new VtiUserExitResult(999, "Error Selecting LDB Item");
			
            itemULdbRows[0].setDoubleFieldValue("ITEM_QTY", newQty);
            itemULdbRows[0].setDoubleFieldValue("ITEM_TOTAL", discCalc);
			itemULdbRows[0].setDoubleFieldValue("DISCOUNT", discVal);
			
			if(discCalc > 0)
			{
				try
				{
					docItemsLdbTable.saveRow(itemULdbRows[0]);
				}
				catch(VtiExitException ee)
				{
					Log.error("Error Saving LDB Row", ee);
					return new VtiUserExitResult(999, "Error Updating LDB Item Row");
				}
			}
            
				
//Ryno Venter stop
				
                // Update the Screen fields with current item.
                currValueField.setDoubleFieldValue(itemTotal);
                itemUpdated = true;
                break;
            }
        }
    }
    
    // If the material was not added, create a new item.
    if(!itemUpdated)
		return new VtiUserExitResult(999, "Failed to update the item");

    
    // Update the Order Total and Order Tax Fields.
    for (int i = 0; i < itemsScreenTable.getRowCount(); ++i)
    {   
        
        VtiUserExitScreenTableRow currentRow = itemsScreenTable.getRow(i);                
        
        orderTotal += currentRow.getDoubleFieldValue("VALUE");
			
		rowDisc = currentRow.getDoubleFieldValue("DISCOUNT_VAL");
		totalDisc = totalDisc + rowDisc;
		discT.setDoubleFieldValue(totalDisc);
    }
    
	orderTax  = orderTotal * (tax * hasTax);   
	orderTotal += orderTax;
    orderTotalField.setDoubleFieldValue(orderTotal);
    orderTaxField.setDoubleFieldValue(orderTax);
    
    headerLdbRow.setDoubleFieldValue("ORDER_TOTAL", orderTotal);
    headerLdbRow.setDoubleFieldValue("ORDER_GST", orderTax);
    headerLdbRow.setDoubleFieldValue("TOTAL_DISC", totalDisc);
    headerLdbRow.setFieldValue("TIMESTAMP", "INPROGRESS");
    
    try
    {
        docHeaderLdbTable.saveRow(headerLdbRow);
    }
    catch (VtiExitException ee)
    {
        Log.error("Error Saving Doc Header Row", ee);
        return new VtiUserExitResult (999, "Error Saving the Order");
    }    
    
		scanItemField.setFieldValue("");
		setCursorPosition(scanItemField);
	}


	if(sCon.equalsIgnoreCase("CASH SALE"))
	{
		cust.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
		cust.setFieldValue("");		
	    setCursorPosition(item);		
	}
	if(sCon.equalsIgnoreCase("STAFF SALE"))
	{
		cust.setDisplayOnlyFlag(false);
		empNo.setDisplayOnlyFlag(false);		
		costCent.setDisplayOnlyFlag(false);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(cust);	
	}
	if(sCon.equalsIgnoreCase("TELLY WALKER"))
	{
		cust.setDisplayOnlyFlag(false);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(cust);
	}
	if(sCon.equalsIgnoreCase("TELLY POINT"))
	{
		cust.setDisplayOnlyFlag(false);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(cust);		
	}
	if(sCon.equalsIgnoreCase("ICMS PAYMENT"))
	{
		cust.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
		cust.setFieldValue("");		
		ordTyp.setFieldValue("CASH SALE");		
	    setCursorPosition(item);		
	}
	if(sCon.equalsIgnoreCase("TAX EXEMPT"))
	{
		cust.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
		cust.setFieldValue("");		
	    setCursorPosition(item);		
	}
	if(sCon.equalsIgnoreCase("RETURN"))
	{
		cust.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
		cust.setFieldValue("");		
	    setCursorPosition(item);		
	}

	currValueField.setDisplayOnlyFlag(true);
	
/*
//End Clear
	
	tellTyp.setFieldValue(sCon);
	String sStaff = staff.getFieldValue();
	
//  Clear Screen fields
	cust.setFieldValue("");
	empNo.setFieldValue("");
	costCent.setFieldValue("");
	serialNo.setFieldValue("");
	name1.setFieldValue("");
	item.setFieldValue("");
	
	if(sCon.equalsIgnoreCase("CASH SALE"))
	{
		cust.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(item);		
	}
	if(sCon.equalsIgnoreCase("STAFF SALE"))
	{
    	name1.setFieldValue("STAFF CUSTOMER");
	    cust.setFieldValue(sStaff);
		cust.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(false);		
		costCent.setDisplayOnlyFlag(false);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(empNo);	
	}
	if(sCon.equalsIgnoreCase("TELLY WALKER"))
	{
		cust.setDisplayOnlyFlag(false);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(cust);
	}
	if(sCon.equalsIgnoreCase("TELLY POINT"))
	{
		cust.setDisplayOnlyFlag(false);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(cust);		
	}
	if(sCon.equalsIgnoreCase("ICMS"))
	{
		cust.setDisplayOnlyFlag(false);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
		setCursorPosition(cust);
	}
	if(sCon.equalsIgnoreCase("TAX EXEMPT"))
	{
		cust.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(item);		
	}
	if(sCon.equalsIgnoreCase("RETURN"))
	{
		cust.setDisplayOnlyFlag(true);
		empNo.setDisplayOnlyFlag(true);		
		costCent.setDisplayOnlyFlag(true);
		serialNo.setDisplayOnlyFlag(true);
	    setCursorPosition(item);		
	}*/
	return new VtiUserExitResult();
  }
}
