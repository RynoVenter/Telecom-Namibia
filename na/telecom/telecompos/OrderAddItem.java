package na.telecom.telecompos;

import java.util.*;
import java.text.*;
import java.math.BigDecimal;
import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.awt.AWTException;
//import java.awt.Robot;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class OrderAddItem extends VtiUserExit
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
	VtiUserExitScreenField printOrdNo = getScreenField("PRINT_ORDNO");
	VtiUserExitScreenField ordTypCd = getScreenField("ORD_TP_CD");
	VtiUserExitScreenField discT = getScreenField("DISCOUNT");
	VtiUserExitScreenField costCent = getScreenField("KOSTL");
	VtiUserExitScreenField ordTyp = getScreenField("ORDTYP");
	VtiUserExitScreenField cust = getScreenField("KUNNR");
	VtiUserExitScreenField custN = getScreenField("NAME1");
	VtiUserExitScreenField serialNo = getScreenField("SERIAL_NO");
	VtiUserExitScreenField gVat = getScreenField("G_VAT");
	VtiUserExitScreenField currMatNo = getScreenField("CURR_MATNR");
	VtiUserExitScreenField prChd = getScreenField("G_PRCHD");
	VtiUserExitScreenField empNo = getScreenField("EMP_NO");
	VtiUserExitScreenField date = getScreenField("DATE");
	VtiUserExitScreenField user = getScreenField("USERID");
	VtiUserExitScreenField test = getScreenField("TEST");
	VtiUserExitScreenField wrkFEanScan = getScreenField("CURRSCANITEM");
	
	if (empNo == null) return new VtiUserExitResult(999, "Screen Field EMP_NO does not exist");
	if (prChd == null) return new VtiUserExitResult(999, "Screen Field G_PRCHD does not exist");
    if (orderNoField == null) return new VtiUserExitResult(999, "Screen Field ORDER_NO does not exist");
    if (orderTotalField == null) return new VtiUserExitResult(999, "Screen Field ORDER_VALUE does not exist");
    if (orderTaxField == null) return new VtiUserExitResult(999, "Screen Field ORDER_TAX does not exist");
    if (scanItemField == null) return new VtiUserExitResult(999, "Screen Field ITEM does not exist");
    if (currDescField == null) return new VtiUserExitResult(999, "Screen Field CURR_MAT_DESC does not exist");
    if (currQtyField == null) return new VtiUserExitResult(999, "Screen Field CURR_QTY does not exist");
    if (currValueField == null) return new VtiUserExitResult(999, "Screen Field CURR_VALUE does not exist");
    if (currItemNoField == null) return new VtiUserExitResult(999, "Screen Field CURR_ITEM_NO does not exist");
	if (printOrdNo == null) return new VtiUserExitResult(999, "Screen Field PRINT_ORDNO does not exist");
    if (ordTypCd == null) return new VtiUserExitResult(999, "Screen Field ORD_TP_CD does not exist");
	if (discT == null) return new VtiUserExitResult(999, "Screen Field DISCOUNT does not exist");
	if (costCent == null) return new VtiUserExitResult(999, "Screen Field KOSTL does not exist");
	if (ordTyp == null) return new VtiUserExitResult(999, "Screen Field ORDTYP does not exist");
	if (cust == null) return new VtiUserExitResult(999, "Screen Field KUNNR does not exist");
	if (serialNo == null) return new VtiUserExitResult(999, "Screen Field SERIAL_NO does not exist");
	if (gVat == null)return new VtiUserExitResult(999, "Screen Field G_VAT does not exist");
	if (currMatNo == null) return new VtiUserExitResult(999, "Screen Field CURR_MATNR does not exist");
	if (date == null) return new VtiUserExitResult(999, "Screen Field DATE does not exist");
	if (user == null) return new VtiUserExitResult(999, "Screen Field USERID does not exist");
	if (wrkFEanScan == null) return new VtiUserExitResult(999, "Screen Field CURRSCANITEM does not exist");
	
    VtiUserExitScreenTable itemsScreenTable = getScreenTable("ITEMS");

    if (itemsScreenTable == null) return new VtiUserExitResult(999, "Screen table ITEMS not found");
    
    VtiExitLdbTable docHeaderLdbTable = getLocalDatabaseTable("YSPS_DOC_HEADER");
    VtiExitLdbTable docItemsLdbTable = getLocalDatabaseTable("YSPS_DOC_ITEMS");
    VtiExitLdbTable materialLdbTable = getLocalDatabaseTable("YSPS_MATERIAL");
    VtiExitLdbTable discountLdbTable = getLocalDatabaseTable("YSPS_DISCOUNT");
	VtiExitLdbTable floatLdbTable = getLocalDatabaseTable("YSPS_FLOAT");
	
    if (docHeaderLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_DOC_HEADER not found");
    if (docItemsLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_DOC_ITEMS not found");
    if (materialLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_MATERIAL not found");
	if (discountLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_DISCOUNT not found");
	if (floatLdbTable == null) return new VtiUserExitResult(999, "LDB YSPS_FLOAT does not exist");

    String vtiServerId = getVtiServerId();
    String orderNo = orderNoField.getFieldValue();
    String scanItem = scanItemField.getFieldValue();
    String material = "";
    String itemNo = "";
	String mtart = "";
	StringBuffer posMes = new StringBuffer();;
    
    double orderTotal = 0;
    double orderTax = 0;
	String printOrdNum;
	int curRow = 0;
	double disc = 0;
	double discCalc = 0;
	double discVal = 0;
	double totalDisc = 0;
	double rowDisc = 0;
	final String ifBlankCheck = "";
	double hasTax = 1;
	int sel = 0;
	double tax = gVat.getDoubleFieldValue();
	boolean adAsNewLine = false;
	boolean useMat = false;
	boolean useOld = true;
	double screenPrice = currValueField.getDoubleFieldValue();
	double taxCor = 0; 

	currQtyField.setDisplayOnlyFlag(false);
	currValueField.setDisplayOnlyFlag(true);
	
	prChd.setFieldValue("");

	if(prChd.getFieldValue().equals("X"))
		useOld = false;
		
	wrkFEanScan.setFieldValue(scanItemField.getFieldValue());
	//Validate that the required info has been logged.
	String sCon = ordTyp.getFieldValue();
	if(sCon.equalsIgnoreCase("CASH SALE"))
	{
		hasTax = 1;
	}
	if(sCon.equalsIgnoreCase("STAFF SALE"))
	{
		hasTax = 1;
		setCursorPosition(empNo);

		if(ifBlankCheck.equals(empNo.getFieldValue()) || ifBlankCheck.equals(costCent.getFieldValue()))
			return new VtiUserExitResult(999,"Please enter a Employee number and Cost Center for the Staff Sale.");
	}
	if(sCon.equalsIgnoreCase("TELLY WALKER"))
	{
		hasTax = 1;
		setCursorPosition(cust);
		
		if(ifBlankCheck.equals(cust.getFieldValue()))
			return new VtiUserExitResult(999,"Please enter a customer number for the Tellywalker.");
	}
	if(sCon.equalsIgnoreCase("TELLY POINT"))
	{
		hasTax = 1;
		setCursorPosition(cust);
		
		if(ifBlankCheck.equals(cust.getFieldValue()))
			return new VtiUserExitResult(999,"Please enter a customer number for the Tellypoint.");
	}
	if(sCon.equalsIgnoreCase("ICMS"))
	{
		hasTax = 1;
		setCursorPosition(cust);
		
		if(ifBlankCheck.equals(cust.getFieldValue()))
			return new VtiUserExitResult(999,"Please enter a customer number for the Government.");
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
			VtiExitLdbSelectCriterion[] floatSelConds =
				{
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId),
					new VtiExitLdbSelectCondition("ONDATE", VtiExitLdbSelectCondition.EQ_OPERATOR, date.getFieldValue()),
					new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, user.getFieldValue())
				};
					
			VtiExitLdbSelectConditionGroup floatSelCondsGrp = new VtiExitLdbSelectConditionGroup(floatSelConds, true);
  
			VtiExitLdbTableRow [] floatLdbRows = floatLdbTable.getMatchingRows(floatSelCondsGrp);
			
			if(floatLdbRows.length == 0)
				return new VtiUserExitResult(999, "Please allocate float for this user.");
            order = getNextNumberFromNumberRange("YSPS_ORDER");
        }
        catch (VtiExitException ee)
        {
            return new VtiUserExitResult(999, "Failed to Get Order Number");
        }
        
        orderNo = Long.toString(order);
        orderNoField.setFieldValue(orderNo);
		printOrdNum = orderNo + "-" + vtiServerId;
		printOrdNo.setFieldValue(printOrdNum);
    }
    
	
    // Select the document header record and see if it exists
    VtiExitLdbSelectCriterion[] headerSelConds =
    {
		new VtiExitLdbSelectCondition("VTI_REF",VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
		new VtiExitLdbSelectCondition("SERVERID",VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId)
    };
        
    VtiExitLdbSelectConditionGroup headerSelCondGrp = new VtiExitLdbSelectConditionGroup(headerSelConds, true);
  
    VtiExitLdbTableRow[] headerLdbRows = docHeaderLdbTable.getMatchingRows(headerSelCondGrp);

    VtiExitLdbTableRow headerLdbRow = null;
    
    // If the record does not exist yet then create the new Row and update the basics order information for the new order
    if (headerLdbRows.length == 0)
    {
        headerLdbRow = docHeaderLdbTable.newRow();
        headerLdbRow.setFieldValue("DOC_TYPE", sCon);
        headerLdbRow.setFieldValue("VTI_REF", orderNo);
        headerLdbRow.setFieldValue("SERVERID", vtiServerId);
        headerLdbRow.setFieldValue("TIMESTAMP", "INPROGRESS");
    }
    else
        headerLdbRow = headerLdbRows[0];

    // If no Item Number "scanned" we can exit.
    if (scanItem.equals(""))
    {

        setCursorPosition(scanItemField);
        return new VtiUserExitResult();
    }   

    // Try to select the entered item from the LDB.  First try EAN, then Material.  
    
    VtiExitLdbSelectCriterion eanSelConds = new VtiExitLdbSelectCondition("EAN", VtiExitLdbSelectCondition.EQ_OPERATOR, scanItem);
    
    VtiExitLdbTableRow[] materialLdbRows = materialLdbTable.getMatchingRows(eanSelConds);
    
    // If this fails, try the material number.
    if (materialLdbRows.length == 0)
    {
			VtiExitLdbSelectCriterion materialSelConds = new VtiExitLdbSelectCondition("MATERIAL", VtiExitLdbSelectCondition.EQ_OPERATOR, scanItem);
        
			materialLdbRows = materialLdbTable.getMatchingRows(materialSelConds);
			useMat = true;
    }
    
    if (materialLdbRows.length == 0)
	{
		setCursorPosition(scanItemField);
        return new VtiUserExitResult(999, "EAN/Material Not Found in DB");
	}
    // Get the Material Number.
    material = materialLdbRows[0].getFieldValue("MATERIAL");
	
    
  
    
//  Get the category - BERNARD STANTON 05052008
    mtart = materialLdbRows[0].getFieldValue("MTART");
	
	currMatNo.setFieldValue(material);
//Check if this material requires a serial number.
	
	VtiExitLdbSelectConditionGroup serialSelCondGrp;
	
	if(useMat)
		{
			VtiExitLdbSelectCriterion [] serialSelConds = 
			{
				new VtiExitLdbSelectCondition("MATERIAL", VtiExitLdbSelectCondition.EQ_OPERATOR, scanItem),
				new VtiExitLdbSelectCondition("SERIAL_NO_REQD", VtiExitLdbSelectCondition.EQ_OPERATOR, "X")
			};
			
			serialSelCondGrp = new VtiExitLdbSelectConditionGroup(serialSelConds, true);
		}
	else
	  {
			 VtiExitLdbSelectCriterion [] serialSelConds = 
			{
				new VtiExitLdbSelectCondition("EAN", VtiExitLdbSelectCondition.EQ_OPERATOR, scanItem),
				new VtiExitLdbSelectCondition("SERIAL_NO_REQD", VtiExitLdbSelectCondition.EQ_OPERATOR, "X")
			};
			 
			 serialSelCondGrp = new VtiExitLdbSelectConditionGroup(serialSelConds, true);
	  }
   
    VtiExitLdbTableRow[] serialLdbRows = materialLdbTable.getMatchingRows(serialSelCondGrp);
	
	if(serialLdbRows.length > 0 && useOld == true)
	{
		if(serialLdbRows[0].getFieldValue("SERIAL_NO_REQD").equals("X") && serialNo.getFieldValue().equals(""))
			{
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

    // Now Loop through the Screen Table to see if it exists.  If yes add 1
    // to Qty in Table and LDB Item. 
    
    boolean materialAdded = false;
    
    if(adAsNewLine == false)
	{
    for (int i = 0; i < itemsScreenTable.getRowCount(); ++i)
    {   
        
        VtiUserExitScreenTableRow currentRow = itemsScreenTable.getRow(i);                
        
        if (currentRow.getFieldValue("MATERIAL").equals(material))
        {

            int newQty = currentRow.getIntegerFieldValue("QTY") + 1; 
			itemNo = Long.toString(currentRow.getLongFieldValue("ITEM_NO"));
            // Select the LDB and update the QTY.
            VtiExitLdbSelectCriterion[] itemSelConds =
            {
                new VtiExitLdbSelectCondition("VTI_REF", VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
                new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId),
                new VtiExitLdbSelectCondition("ITEM_NO", VtiExitLdbSelectCondition.EQ_OPERATOR, itemNo)
            };
   
            VtiExitLdbSelectConditionGroup itemSelCondGrp = new VtiExitLdbSelectConditionGroup(itemSelConds, true);
  
            VtiExitLdbTableRow[] itemLdbRows = docItemsLdbTable.getMatchingRows(itemSelCondGrp);
			
            if (itemLdbRows.length == 0) return new VtiUserExitResult(999, "Error Selecting LDB Item");

			double itemTotal = 0;
            if(useOld)
				{
						itemTotal = itemLdbRows[0].getDoubleFieldValue("ACTPRICE_INCTAX") * newQty;
				}
				else
				{
					itemTotal = screenPrice;
				}
            itemLdbRows[0].setIntegerFieldValue("ITEM_QTY", newQty);
            itemLdbRows[0].setDoubleFieldValue("ITEM_TOTAL", itemTotal);
			itemLdbRows[0].setFieldValue("VTI_REF", orderNo);
			itemLdbRows[0].setFieldValue("ORDER_NO", printOrdNo.getFieldValue()); 
			
			if(serialNo.getFieldValue().length() > 0)
			{
				itemLdbRows[0].setFieldValue("SERIAL_NO_REQD", "X");
				itemLdbRows[0].setFieldValue("SERIAL_NO", serialNo.getFieldValue());
				serialNo.setFieldValue("");
				serialNo.setDisplayOnlyFlag(true);
			}

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
            currentRow.setIntegerFieldValue("QTY", newQty);
            currentRow.setDoubleFieldValue("VALUE", itemTotal);
			currentRow.setDoubleFieldValue("DISCOUNT_VAL", 0);
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
                new VtiExitLdbSelectCondition("VTI_REF",VtiExitLdbSelectCondition.EQ_OPERATOR, orderNo),
                new VtiExitLdbSelectCondition("SERVERID",VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId),
                new VtiExitLdbSelectCondition("ITEM_NO", VtiExitLdbSelectCondition.EQ_OPERATOR, itemNo)
            };
                
            VtiExitLdbSelectConditionGroup itemUSelCondGrp = new VtiExitLdbSelectConditionGroup(itemUSelConds, true);
  
            VtiExitLdbTableRow[] itemULdbRows = docItemsLdbTable.getMatchingRows(itemUSelCondGrp);
            
            if (itemULdbRows.length == 0) return new VtiUserExitResult(999, "Error Selecting LDB Item");
			
            itemULdbRows[0].setIntegerFieldValue("ITEM_QTY", newQty);
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
	        materialAdded = true;
            
            // Update the Screen fields with current item.
            currDescField.setFieldValue(currentRow.getFieldValue("MAT_DESC"));
            currQtyField.setIntegerFieldValue(newQty);
            currValueField.setDoubleFieldValue(itemTotal);
            currItemNoField.setFieldValue(itemNo);
			
            break;
        }
    }
	}
	
  

    // If the material was not added, create a new item.
    if(!materialAdded)
    {
		if(scanItemField.getFieldValue().equals(serialNo.getFieldValue()))
	   {
			setCursorPosition(serialNo);
			return new VtiUserExitResult(999,"Please scan the serial number.");
		}
        long longItemNo = this.getNextNumberFromNumberRange("YSPS_ITEM");
        itemNo = Long.toString(longItemNo);
        
        
        VtiExitLdbTableRow newItemRow = docItemsLdbTable.newRow();
        
        newItemRow.setFieldValue("VTI_REF", orderNo); 
		newItemRow.setFieldValue("ORDER_NO", printOrdNo.getFieldValue()); 
        newItemRow.setFieldValue("SERVERID", vtiServerId);
        newItemRow.setFieldValue("ITEM_NO", itemNo);
        newItemRow.setFieldValue("MATERIAL", material);
        newItemRow.setFieldValue("MAT_DESC", materialLdbRows[0].getFieldValue("MAT_DESC"));
        newItemRow.setFieldValue("EAN", materialLdbRows[0].getFieldValue("EAN"));
		
			if(serialNo.getFieldValue().length() > 0)
			{
				newItemRow.setFieldValue("SERIAL_NO_REQD", "X");
				newItemRow.setFieldValue("SERIAL_NO", serialNo.getFieldValue());
				serialNo.setFieldValue("");
				serialNo.setDisplayOnlyFlag(true);
			}
			
        double rrPrice = materialLdbRows[0].getDoubleFieldValue("RR_PRICE");
		
        newItemRow.setDoubleFieldValue("RR_PRICE", rrPrice);
        newItemRow.setDoubleFieldValue("ACT_PRICE", rrPrice);
        newItemRow.setFieldValue("ITEM_QTY", "1");
                
        double taxPrice = rrPrice;
        newItemRow.setDoubleFieldValue("RRP_INCLTAX", taxPrice);
        newItemRow.setDoubleFieldValue("ACTPRICE_INCTAX", taxPrice);
		
		
//discount		
//Ryno Venter append
			
				// Select the required records.
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
							if(1 >= discountLdbRows[nextDiscCond].getLongFieldValue("KSTBM"))
								disc = discountLdbRows[nextDiscCond].getDoubleFieldValue("KBETR")* -1 / 10;
						}
				}
				else
				{
					VtiExitLdbSelectCriterion [] discount2SelConds =
						{
						new VtiExitLdbSelectCondition("KSCHL",VtiExitLdbSelectCondition.EQ_OPERATOR,ordTypCd.getFieldValue()),
						new VtiExitLdbSelectCondition("KSTBM",VtiExitLdbSelectCondition.LE_OPERATOR,"1"),
						new VtiExitLdbSelectCondition("MATNR",VtiExitLdbSelectCondition.EQ_OPERATOR,""),
						new VtiExitLdbSelectCondition("KUNNR",VtiExitLdbSelectCondition.EQ_OPERATOR,cust.getFieldValue()),
						};
			
					VtiExitLdbSelectConditionGroup discount2SelCondsGrp = new VtiExitLdbSelectConditionGroup(discount2SelConds, true);
  					VtiExitLdbTableRow [] discount2LdbRows = discountLdbTable.getMatchingRows(discount2SelCondsGrp);	
						if(discount2LdbRows.length>0)
						{
							for(int nextDiscCond = 0 ;nextDiscCond < discount2LdbRows.length; nextDiscCond++)
								{
									if(1 >= discount2LdbRows[nextDiscCond].getLongFieldValue("KSTBM"))
										disc = discount2LdbRows[nextDiscCond].getDoubleFieldValue("KBETR")* -1 / 10;
								}
						}
						else
						{
					   				VtiExitLdbSelectCriterion [] discount3SelConds =
										{
											new VtiExitLdbSelectCondition("KSCHL",VtiExitLdbSelectCondition.EQ_OPERATOR,ordTypCd.getFieldValue()),
											new VtiExitLdbSelectCondition("KSTBM",VtiExitLdbSelectCondition.LE_OPERATOR,"1"),
											new VtiExitLdbSelectCondition("MATNR",VtiExitLdbSelectCondition.EQ_OPERATOR,material),
											new VtiExitLdbSelectCondition("KUNNR",VtiExitLdbSelectCondition.EQ_OPERATOR,""),
										};
			
										VtiExitLdbSelectConditionGroup discount3SelCondsGrp = new VtiExitLdbSelectConditionGroup(discount3SelConds, true);
  										VtiExitLdbTableRow [] discount3LdbRows = discountLdbTable.getMatchingRows(discount3SelCondsGrp);	
										if(discount3LdbRows.length>0)
										{
												for(int nextDiscCond = 0 ;nextDiscCond < discount3LdbRows.length; nextDiscCond++)
												{
													if(1 >= discount3LdbRows[nextDiscCond].getLongFieldValue("KSTBM"))
															disc = discount3LdbRows[nextDiscCond].getDoubleFieldValue("KBETR")* -1 / 10;
												}
										}
						}
				}
				
				if (disc > 0)
				{
					discCalc = rrPrice;
					discVal = discCalc * disc / 100;
					discCalc = discCalc - discVal;
				}
				else
				{
					discCalc = rrPrice;
				}
//discount	
				
				
        newItemRow.setDoubleFieldValue("ITEM_TOTAL", discCalc);                
        newItemRow.setFieldValue("TIMESTAMP", "INPROGRESS"); 
		newItemRow.setDoubleFieldValue("DISCOUNT", discVal);

//			if(discCalc > 0)
//			{
				try
				{
					docItemsLdbTable.saveRow(newItemRow);
				}
				catch(VtiExitException ee)
				{
					Log.error("Error Saving New Item LDB Row", ee);
					return new VtiUserExitResult(999, "Error Updating New Item LDB Row");
				}
//			}
        
        // Append the Screen Table
        VtiUserExitScreenTableRow newRow = itemsScreenTable.getNewRow();

		newRow.setFieldValue("MATERIAL", material);
        newRow.setFieldValue("MAT_DESC", materialLdbRows[0].getFieldValue("MAT_DESC"));
        newRow.setFieldValue("ITEM_NO", itemNo);
        newRow.setFieldValue("QTY", "1");
		if (disc > 0)
			{
				newRow.setDoubleFieldValue("DISCOUNT_VAL",discVal);
				newRow.setDoubleFieldValue("VALUE",discCalc);
			}
		else
			{
				newRow.setDoubleFieldValue("VALUE", taxPrice);
			}
        itemsScreenTable.appendRow(newRow); 
        
        // Update the screen fields of current item.
        currDescField.setFieldValue(materialLdbRows[0].getFieldValue("MAT_DESC"));
        currQtyField.setIntegerFieldValue(1);
        currValueField.setDoubleFieldValue(taxPrice);
        currItemNoField.setFieldValue(itemNo);
    }
    
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
		  if(rowDisc == 0 && hasTax == 1)
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
    }
           
	orderTax  = orderTotal * (tax * hasTax) + taxCor;   
	orderTotal += orderTax;
	
    orderTotalField.setDoubleFieldValue(orderTotal);
    orderTaxField.setDoubleFieldValue(orderTax);
    
    //Display order total to the price poll
//    String strPricePollMessage = new String();
//    strPricePollMessage = "Total $"+orderTotal;
//    
//    try{
//    	PricePoll.DisplayText(this, strPricePollMessage);
//	}
//    catch(VtiExitException e)
//    {
//    	return new VtiUserExitResult(999, e.getMessage());
//    }
    
    headerLdbRow.setDoubleFieldValue("ORDER_TOTAL", orderTotal);
	headerLdbRow.setDoubleFieldValue("TOTAL_DISC", totalDisc);
	headerLdbRow.setFieldValue("EMPNO", empNo.getFieldValue());
	headerLdbRow.setFieldValue("KUNNR", cust.getFieldValue());
	headerLdbRow.setFieldValue("KOSTL", costCent.getFieldValue());
	headerLdbRow.setFieldValue("ORDER_NO", printOrdNo.getFieldValue());
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
  	
//  BERNARD STANTON - 05052008
	if (mtart.equals("DIEN"))
	{  
	  //currQtyField.setIntegerFieldValue(1);	
	  //currQtyField.setDisplayOnlyFlag(true);
	  currValueField.setDisplayOnlyFlag(false);
      scanItemField.setFieldValue("");	  
	  setCursorPosition(currValueField);
	}
	else
	{	
    scanItemField.setFieldValue("");
    setCursorPosition(scanItemField);
	}
	prChd.setFieldValue("");
	if(currQtyField.getLongFieldValue() > 1)
		currValueField.setDisplayOnlyFlag(true);
	//POSPOLE Print
	if(currQtyField.getIntegerFieldValue() == 1)
	   	posMes.append(makePOSLine(currDescField.getFieldValue(), (currValueField.getDoubleFieldValue() * gVat.getDoubleFieldValue() * hasTax) + currValueField.getDoubleFieldValue()));
		//Preparing POS Message	
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
	
	String hostName = getHostInterfaceName();
	boolean hostConnected = isHostInterfaceConnected(hostName);
	
	if(hostConnected)
			return new VtiUserExitResult();
		else
			return new VtiUserExitResult(000,"Host not connected.");

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
