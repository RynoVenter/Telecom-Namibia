package na.telecom.telecompos;

import java.util.*;
import java.text.*;
import java.math.BigDecimal;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class OrderUpdateItem extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
    // Data Declarations.  
    VtiUserExitScreenField orderNoField = getScreenField("ORDER_NO");
    VtiUserExitScreenField orderTotalField = getScreenField("ORDER_VALUE");
    VtiUserExitScreenField orderTaxField = getScreenField("ORDER_TAX");
    VtiUserExitScreenField scanItemField = getScreenField("ITEM");
    VtiUserExitScreenField currDescField = getScreenField("CURR_MAT_DESC");
    VtiUserExitScreenField currQtyField = getScreenField("CURR_QTY");
    VtiUserExitScreenField currValueField = getScreenField("CURR_VALUE");
    VtiUserExitScreenField currItemNoField = getScreenField("CURR_ITEM_NO");
	VtiUserExitScreenField ordTyp = getScreenField("ORDTYP");
	VtiUserExitScreenField ordTypCd = getScreenField("ORD_TP_CD");
	VtiUserExitScreenField gVat = getScreenField("G_VAT");
	VtiUserExitScreenField discT = getScreenField("DISCOUNT");
	VtiUserExitScreenField serialNo = getScreenField("SERIAL_NO");
	VtiUserExitScreenField cust = getScreenField("KUNNR");
	VtiUserExitScreenField currMatNo = getScreenField("CURR_MATNR");
	VtiUserExitScreenField prChd = getScreenField("G_PRCHD");
	VtiUserExitScreenField test = getScreenField("TEST");
	VtiUserExitScreenField wrkFEanScan = getScreenField("CURRSCANITEM");
	
	if (currMatNo == null) return new VtiUserExitResult(999, "Screen Field CURR_MATNR does not exist");
	if (orderNoField == null) return new VtiUserExitResult(999, "Screen Field ORDER_NO does not exist");
    if (orderTotalField == null) return new VtiUserExitResult(999, "Screen Field ORDER_VALUE does not exist");
    if (orderTaxField == null) return new VtiUserExitResult(999, "Screen Field ORDER_TAX does not exist");
    if (scanItemField == null) return new VtiUserExitResult(999, "Screen Field ITEM does not exist");
    if (currDescField == null) return new VtiUserExitResult(999, "Screen Field CURR_MAT_DESC does not exist");
    if (currQtyField == null) return new VtiUserExitResult(999, "Screen Field CURR_QTY does not exist");
    if (currValueField == null) return new VtiUserExitResult(999, "Screen Field CURR_VALUE does not exist");
    if (currItemNoField == null) return new VtiUserExitResult(999, "Screen Field CURR_ITEM_NO does not exist");
     if (ordTyp == null) return new VtiUserExitResult(999, "Screen Field ORDTYP does not exist");
    if (ordTypCd == null) return new VtiUserExitResult(999, "Screen Field ORD_TP_CD does not exist");
	if (serialNo == null) return new VtiUserExitResult(999, "Screen Field SERIAL_NO does not exist");
	if (cust == null) return new VtiUserExitResult(999, "Screen Field customer does not exist");
	if (prChd == null) return new VtiUserExitResult(999, "Screen Field G_PRCHD does not exist");
	if (wrkFEanScan == null) return new VtiUserExitResult(999, "Screen Field CURRSCANITEM does not exist");
	
    VtiUserExitScreenTable itemsScreenTable = getScreenTable("ITEMS");

    if (itemsScreenTable == null) return new VtiUserExitResult (999, "Screen table ITEMS not found");

    
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
    String scanItem = scanItemField.getFieldValue();
    String material = "";
    String itemNo = currItemNoField.getFieldValue();
	StringBuffer posMes = new StringBuffer();

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
	double taxCor = 0;	
	
	if(wrkFEanScan.getFieldValue().length() == 0)
		return new VtiUserExitResult(999,"Re-scan the Item please, then update the qty.");
	if(prChd.getFieldValue().equals("X"))
		useOld = false;
	
	String materialNum = "";
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
        return new VtiUserExitResult(999, "Failed to Get Order Number");      
    
    if (newQty == 0)
    {
        
    }

    
    // Select the document header record.
    VtiExitLdbSelectCriterion[] headerSelConds =
    {
    new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
    new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
    };
        
    VtiExitLdbSelectConditionGroup headerSelCondGrp = new VtiExitLdbSelectConditionGroup(headerSelConds, true);
  
    VtiExitLdbTableRow[] headerLdbRows = docHeaderLdbTable.getMatchingRows(headerSelCondGrp);

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
	{
		setCursorPosition(scanItemField);
			return new VtiUserExitResult(999, "EAN/Material Not Found in DB");
	}
    
    // Get the Material Number.
    material = materialLdbRows[0].getFieldValue("MATERIAL");
	

    // Now Loop through the Screen Table to update the item values.
    
    boolean itemUpdated = false;
    
    for (int i = 0; i < itemsScreenTable.getRowCount(); ++i)
    {   
        
        VtiUserExitScreenTableRow currentRow = itemsScreenTable.getRow(i);                
        
        if (currentRow.getFieldValue("ITEM_NO").equals(itemNo))
        {                     
                       
            // Select the LDB and update the QTY.
            VtiExitLdbSelectCriterion[] itemSelConds =
            {
                new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
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
					if(newQty == 1 && prChd.getFieldValue().equals("X"))
							itemLdbRows[0].setDoubleFieldValue("ACTPRICE_INCTAX",screenPrice);
						else
							screenPrice = itemLdbRows[0].getDoubleFieldValue("ACTPRICE_INCTAX");
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
                currentRow.setDoubleFieldValue("DISCOUNT_VAL", 0);
//Disc append
			
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
            
            if (itemULdbRows.length == 0) return new VtiUserExitResult(999, "Error Selecting LDB Item");
           
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
            
				
//Disc stop
			 currValueField.setDisplayOnlyFlag(true);	
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
        rowDisc = 0;
		
        VtiUserExitScreenTableRow currentRow = itemsScreenTable.getRow(i);                
        
        orderTotal += currentRow.getDoubleFieldValue("VALUE");
			
		rowDisc = currentRow.getDoubleFieldValue("DISCOUNT_VAL");
		
		double itemSubTotal = currentRow.getDoubleFieldValue("VALUE");
		double iTax = itemSubTotal * tax;
		BigDecimal rndItemTotal =  new BigDecimal(Double.toString(itemSubTotal + iTax));
		rndItemTotal = rndItemTotal.setScale(2,BigDecimal.ROUND_UP);
		double itemTotal = itemSubTotal + iTax;//rndItemTotal.doubleValue();
		double div = itemTotal % 1;
		double div_100 = 1-div;
		String aCard = "";
		
		VtiExitLdbSelectCriterion [] isACard = 
			{
				new VtiExitLdbSelectCondition("MATERIAL", VtiExitLdbSelectCondition.EQ_OPERATOR, currentRow.getFieldValue("MATERIAL")),
					new VtiExitLdbSelectCondition("MATKL", VtiExitLdbSelectCondition.EQ_OPERATOR, "19")
		};
		
		VtiExitLdbSelectConditionGroup isACardGrp = new VtiExitLdbSelectConditionGroup(isACard, true);
		
		VtiExitLdbTableRow[] isACardLDBRow = materialLdbTable.getMatchingRows(isACardGrp);
		
		if(isACardLDBRow.length > 0)
		{
		  if(rowDisc == 0 && hasTax > 0)
		  {
			if(div<0.5)
			{
				if(div_100<0.5)
				{
					taxCor = div;
				}
				else
				{
					taxCor = -div;
				}
			}
			else
			{
				if(div_100<0.5)
				{
					taxCor = div_100;
				}
				else
				{
					taxCor = -div_100;
				}
			}
		  }
		}

		totalDisc = totalDisc + rowDisc;
		discT.setDoubleFieldValue(totalDisc);
		//return new VtiUserExitResult(999,rowDisc + " " + hasTax + " " + aCard);
    }
    
	orderTax  = orderTotal * (tax * hasTax) + taxCor;   
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
        return new VtiUserExitResult
            (999, "Error Saving the Order");
    }    
	
	//POSPOLE Print
	if(currQtyField.getIntegerFieldValue() == 1)
	   	posMes.append(makePOSLine(currDescField.getFieldValue(), (currValueField.getDoubleFieldValue() * gVat.getDoubleFieldValue() * hasTax) + currValueField.getDoubleFieldValue()));
			//Prepare POS Message
			if(currQtyField.getIntegerFieldValue() > 1 && discCalc == 0)
				posMes.append(makePOSLine(currDescField.getFieldValue(), (currValueField.getDoubleFieldValue() * gVat.getDoubleFieldValue() * hasTax)  + currValueField.getDoubleFieldValue()));
			else if(currQtyField.getIntegerFieldValue() > 1 && discCalc > 0)
				posMes.append(makePOSLine(currDescField.getFieldValue(), (discCalc * gVat.getDoubleFieldValue() * hasTax)  + discCalc));

	
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
    
    scanItemField.setFieldValue("");
    setCursorPosition(scanItemField);
	prChd.setFieldValue("");
    
	return new VtiUserExitResult();

  }
  
	private StringBuffer makePOSLine(String desc,  double amnt)
	{
		StringBuffer makeLI = new StringBuffer();
		String lineReturn = System.getProperty("line.separator");
		String pos20 = "";
		if(desc.length() < 20)
			pos20 = desc.substring(0,desc.length());
		else
			pos20 = desc.substring(0,20);
		
		DecimalFormat df1 = new DecimalFormat("######0.00");
		
		makeLI.append(pos20 + fillSpace(pos20.length()));
		makeLI.append("Value : ");
		makeLI.append(df1.format(amnt));

		return makeLI;
	}
	
	public String fillSpace(int length)
	{
		//Attributes
		final String space = " ";
		String differSpace = "";
		int amtSpace = 20;//20 char per PosPole line
		int createSpace = 1;
		
		amtSpace = amtSpace - length;
		
		while(createSpace <= amtSpace)
		{
			createSpace++;
			differSpace = differSpace + space;
		}
		return differSpace;
	}
}
