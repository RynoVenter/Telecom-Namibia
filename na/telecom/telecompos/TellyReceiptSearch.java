package na.telecom.telecompos;

import java.util.Date;

import au.com.skytechnologies.ecssdk.util.DateFormatter;
import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitLdbOrderSpecification;
import au.com.skytechnologies.vti.VtiExitLdbSelectCondition;
import au.com.skytechnologies.vti.VtiExitLdbSelectConditionGroup;
import au.com.skytechnologies.vti.VtiExitLdbSelectCriterion;
import au.com.skytechnologies.vti.VtiExitLdbTable;
import au.com.skytechnologies.vti.VtiExitLdbTableRow;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;
import au.com.skytechnologies.vti.VtiUserExitScreenTableRow;
import au.com.skytechnologies.vti.VtiUserExitStyle;

public class TellyReceiptSearch extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Data Declarations.     
		VtiUserExitScreenField Deposit = getScreenField("DEP");	  
		VtiUserExitScreenField ExpectedCash = getScreenField("EXP_CASH");
		VtiUserExitScreenField Discount = getScreenField("DISC");
		VtiUserExitScreenField AmountDue = getScreenField("AMOUNT_DUE");
		VtiUserExitScreenField TellyNo = getScreenField("TELLY_NO");
		VtiUserExitScreenField TellyName = getScreenField("TELLY_NAME");
		VtiUserExitScreenField TellyType = getScreenField("TELLTYPE");
		VtiUserExitScreenField CurrCust = getScreenField("CURRENT_CUST");
		VtiUserExitScreenField DueDays = getScreenField("DUE_DAYS");
		VtiUserExitScreenField Tax = getScreenField("TAX");
		VtiUserExitScreenTable AllocScrTable = getScreenTable("ALLOC");

		VtiExitLdbTable YSPS_TELLY_CUST = getLocalDatabaseTable("YSPS_TELLY_CUST");
		VtiExitLdbTable YSPS_MVMT_HEADER = getLocalDatabaseTable("YSPS_MVMT_HEADER");
		VtiExitLdbTable YSPS_MVMT_ITEMS = getLocalDatabaseTable("YSPS_MVMT_ITEMS");
		VtiExitLdbTable YSPS_GEN_CONFIG = getLocalDatabaseTable("YSPS_GENERAL_CONFIGURATION");
		VtiExitLdbTable YSPS_DISCOUNT = getLocalDatabaseTable("YSPS_DISCOUNT");
		VtiExitLdbTable YSPS_MATERIAL = getLocalDatabaseTable("YSPS_MATERIAL");
		
		if (Tax== null)
			return new VtiUserExitResult(999, "Screen Field TAX does not exist");
		if (Deposit == null)
			return new VtiUserExitResult(999, "Screen Field DEPOSIT does not exist");
		if (DueDays== null)
			return new VtiUserExitResult(999, "Screen Field DUE_DAYS does not exist");
	
		if (CurrCust== null)
			return new VtiUserExitResult(999, "Screen Field CURRENT_CUST does not exist");

		if (ExpectedCash == null)
			return new VtiUserExitResult(999, "Screen Field EXP_CASH does not exist");    

		if (Discount== null)
			return new VtiUserExitResult(999, "Screen Field DISC does not exist");

		if (AmountDue== null)
			return new VtiUserExitResult(999, "Screen Field AMOUNT_DUE does not exist");
		if (TellyNo== null)
			return new VtiUserExitResult(999, "Screen Field TELLY_NO does not exist");
		if (TellyName== null)
			return new VtiUserExitResult(999, "Screen Field TELLY_NAME does not exist");

		if (TellyType== null)
			return new VtiUserExitResult(999, "Screen Field TELLTYPE does not exist");

		if (AllocScrTable== null)
			return new VtiUserExitResult(999, "Screen table ALLOC does not exist");

		if (YSPS_TELLY_CUST== null)
			return new VtiUserExitResult(999, "LDB table YSPS_TELLY_CUST does not exist");
		if (YSPS_GEN_CONFIG== null)
			return new VtiUserExitResult(999, "LDB table YSPS_GENERAL_CONFIGURATION does not exist");
		if (YSPS_DISCOUNT== null)
			return new VtiUserExitResult(999, "LDB table YSPS_DISCOUNT does not exist");
		if (YSPS_MATERIAL== null)
			return new VtiUserExitResult(999, "LDB table YSPS_MATERIAL does not exist");

		String strTellyNo = TellyNo.getFieldValue();
		String strTranType 			= "TELLY ISSUE";
		String strDiscount          = "DISCOUNT";
		String strCurrCustomer = CurrCust.getFieldValue();
		int    intDueDays = DueDays.getIntegerFieldValue();
		Date currNow = new Date();
		int currDate = Integer.parseInt(DateFormatter.format("yyyyMMdd", currNow));
		VtiUserExitStyle redStyle = getStyle(1);
		VtiUserExitStyle greenStyle = getStyle(2);
		double dblDiscountPrice = 0;
		double dblDiscountRate = 0; //0.125;
		String strTellyType = TellyType.getFieldValue();
		String strPriceCond = new String();
		double dblTargetSalesQty = 0;
		double dblTax = Tax.getDoubleFieldValue();
		String strDiscountPrice = new String();

//		Get tax value
		VtiExitLdbSelectCriterion[] taxCondSelCond =
		{
				new VtiExitLdbSelectCondition("SERVERID",
						VtiExitLdbSelectCondition.EQ_OPERATOR,""),
						new VtiExitLdbSelectCondition("OBJECT",
								VtiExitLdbSelectCondition.EQ_OPERATOR,"SALES ORDER"),
								new VtiExitLdbSelectCondition("TYPE_CODE",
										VtiExitLdbSelectCondition.EQ_OPERATOR, "VAT"),
										new VtiExitLdbSelectCondition("DEL_IND",
												VtiExitLdbSelectCondition.NE_OPERATOR, "X"),

		};

		VtiExitLdbSelectConditionGroup taxCondSelCondGrp =
			new VtiExitLdbSelectConditionGroup(taxCondSelCond, true);


		VtiExitLdbTableRow[] taxCondLDBRow =
			YSPS_GEN_CONFIG.getMatchingRows(taxCondSelCondGrp);

		if(taxCondLDBRow.length==0)
			return new VtiUserExitResult(999,"Sales VAT value not found in general configuration");

		dblTax = taxCondLDBRow[0].getDoubleFieldValue("ATTRIBUTE1");
		
		Tax.setFieldValue(dblTax);
		
		//Only refresh the list if it is a different customer
		if(!strTellyNo.equals(strCurrCustomer))
		{
			AllocScrTable.clear();
			ExpectedCash.setFieldValue("");
			Discount.setFieldValue("");
			AmountDue.setFieldValue("");
			
			
			//Get pricing condition
			VtiExitLdbSelectCriterion[] priceCondSelCond =
			{
					new VtiExitLdbSelectCondition("SERVERID",
							VtiExitLdbSelectCondition.EQ_OPERATOR,""),
							new VtiExitLdbSelectCondition("OBJECT",
									VtiExitLdbSelectCondition.EQ_OPERATOR,strDiscount),
									new VtiExitLdbSelectCondition("TYPE_CODE",
											VtiExitLdbSelectCondition.EQ_OPERATOR, strTellyType),
											new VtiExitLdbSelectCondition("DEL_IND",
													VtiExitLdbSelectCondition.NE_OPERATOR, "X"),
											
			};

			VtiExitLdbSelectConditionGroup priceCondSelCondGrp =
				new VtiExitLdbSelectConditionGroup(priceCondSelCond, true);


			VtiExitLdbTableRow[] priceCondLDBRow =
				YSPS_GEN_CONFIG.getMatchingRows(priceCondSelCondGrp);
			
			if(priceCondLDBRow.length==0)
				return new VtiUserExitResult(999,"Pricing condition for " + strTellyType + " not found in general configuration");
			
			
			
			strPriceCond = priceCondLDBRow[0].getFieldValue("ATTRIBUTE1");
			//System.out.println("Pricing condition selected is "+strPriceCond);
			
			//Get discount rate
			VtiExitLdbSelectCriterion[] discountRateCondSelCond =
			{
					new VtiExitLdbSelectCondition("KSCHL",
							VtiExitLdbSelectCondition.EQ_OPERATOR, strPriceCond),
							new VtiExitLdbSelectCondition("KUNNR",
									VtiExitLdbSelectCondition.EQ_OPERATOR, strTellyNo),
									new VtiExitLdbSelectCondition("DEL_IND",
											VtiExitLdbSelectCondition.NE_OPERATOR, "X"),
											


			};

			VtiExitLdbSelectConditionGroup discountRateCondSelCondGrp =
				new VtiExitLdbSelectConditionGroup(discountRateCondSelCond, true);


			//Order by sales target quantity ascending order
			VtiExitLdbOrderSpecification discountOrderSpec = 
				new VtiExitLdbOrderSpecification("KSTBM",true);
			
			VtiExitLdbTableRow[] discountCondLDBRow =
				YSPS_DISCOUNT.getMatchingRows(discountRateCondSelCondGrp,discountOrderSpec);
			
			if(discountCondLDBRow.length==0)
				return new VtiUserExitResult(999,"Discount rate does not exist for this customer");
			
			
			
			
			
			
			//get open items
			VtiExitLdbSelectCriterion[] moveHeaderSelCond =
			{
					new VtiExitLdbSelectCondition("KUNNR",
							VtiExitLdbSelectCondition.EQ_OPERATOR, strTellyNo),
							new VtiExitLdbSelectCondition("TRAN_TYPE",
									VtiExitLdbSelectCondition.EQ_OPERATOR, strTranType),


			};

			VtiExitLdbSelectConditionGroup moveHeaderSelCondGrp =
				new VtiExitLdbSelectConditionGroup(moveHeaderSelCond, true);


			VtiExitLdbTableRow[] moveHeaderLDBRows =
				YSPS_MVMT_HEADER.getMatchingRows(moveHeaderSelCondGrp);


			if(moveHeaderLDBRows.length==0)
			{
				CurrCust.setFieldValue("");
				return new VtiUserExitResult(000,"No records selected for this customer");
			}

			//get movement items
			for(int b=0; b<moveHeaderLDBRows.length; b++)
			{

				String strServerGroup = moveHeaderLDBRows[b].getFieldValue("SERVERGROUP");
				String strServerID = moveHeaderLDBRows[b].getFieldValue("SERVERID");
				String strTranNo = moveHeaderLDBRows[b].getFieldValue("TRANS_NO");

				VtiExitLdbSelectCriterion[] moveItemsSelCond =
				{
						new VtiExitLdbSelectCondition("SERVERGROUP",
								VtiExitLdbSelectCondition.EQ_OPERATOR, strServerGroup),
								new VtiExitLdbSelectCondition("SERVERID",
										VtiExitLdbSelectCondition.EQ_OPERATOR, strServerID),
										new VtiExitLdbSelectCondition("TRANS_NO",
												VtiExitLdbSelectCondition.EQ_OPERATOR, strTranNo),
												new VtiExitLdbSelectCondition("FINAL_RECEIPT",
														VtiExitLdbSelectCondition.NE_OPERATOR, "X"),


				};

				VtiExitLdbSelectConditionGroup moveItemsSelCondGrp =
					new VtiExitLdbSelectConditionGroup(moveItemsSelCond, true);


				VtiExitLdbTableRow[] moveItemsLDBRows =
					YSPS_MVMT_ITEMS.getMatchingRows(moveItemsSelCondGrp);


				for(int c=0; c<moveItemsLDBRows.length; c++)
				{

					String moveMatnr = moveItemsLDBRows[c].getFieldValue("MATNR");
					String moveMatnrDesc = moveItemsLDBRows[c].getFieldValue("MAT_DESC");
					int moveQty = moveItemsLDBRows[c].getIntegerFieldValue("MENGE");
					int moveCreDate = moveItemsLDBRows[c].getIntegerFieldValue("CRE_DATE");
					int dueDaysDate = moveCreDate + intDueDays;
					double dblTotalAmount = moveItemsLDBRows[c].getIntegerFieldValue("DMBTR");
					double dblRRP = 0;
					String strPlantFr = moveItemsLDBRows[c].getFieldValue("PLANTFR");
					String strStoreFr = moveItemsLDBRows[c].getFieldValue("STOREFR");
					String strStoreTo = moveItemsLDBRows[c].getFieldValue("STORETO");
					String strTransNo = moveItemsLDBRows[c].getFieldValue("TRANS_NO");
					String strItemNo = moveItemsLDBRows[c].getFieldValue("ITEM_NO");
					
					dblTargetSalesQty = 0;
					dblDiscountRate = 0;
					
					
					//Get RRP from Material
					VtiExitLdbSelectCriterion[] materialSelCond =
					{
							new VtiExitLdbSelectCondition("MATERIAL",
									VtiExitLdbSelectCondition.EQ_OPERATOR, moveMatnr),
														};

					VtiExitLdbSelectConditionGroup materialSelCondGrp =
						new VtiExitLdbSelectConditionGroup(materialSelCond, true);


					VtiExitLdbTableRow[] materialLdbRow =
						YSPS_MATERIAL.getMatchingRows(materialSelCondGrp);
					
					if(materialLdbRow.length==0)
						return new VtiUserExitResult(999,"Material not found in database");
					
					
					
					//Search for discount by quantity being sold
					for(int i=0; i<discountCondLDBRow.length; i++)
					{
						if(discountCondLDBRow[i].getDoubleFieldValue("KSTBM")<=moveQty)
						{
							dblTargetSalesQty = discountCondLDBRow[0].getDoubleFieldValue("KSTBM");
							dblDiscountRate = Math.abs(discountCondLDBRow[0].getDoubleFieldValue("KBETR"));
							
							dblDiscountRate = dblDiscountRate/1000;
							
						}
					}
					
					
					
					if (moveQty>0)
					{
						dblRRP = materialLdbRow[0].getDoubleFieldValue("RR_PRICE");
					}
					else
						dblRRP = 0;
					
					VtiUserExitScreenTableRow receiptItemsScnRow = AllocScrTable.getNewRow();
					receiptItemsScnRow.setFieldValue("ITEM",moveMatnr);
					receiptItemsScnRow.setFieldValue("DESC",moveMatnrDesc);
					receiptItemsScnRow.setFieldValue("QTY",moveQty);
					receiptItemsScnRow.setFieldValue("ISSUEDATE",moveCreDate);
					receiptItemsScnRow.setFieldValue("SOLD",moveQty);
					receiptItemsScnRow.setFieldValue("PROCESSED","");
					receiptItemsScnRow.setFieldValue("DUE_DATE",dueDaysDate);
					receiptItemsScnRow.setFieldValue("RRP",dblRRP);
					receiptItemsScnRow.setFieldValue("PLANT_FR",strPlantFr);
					receiptItemsScnRow.setFieldValue("STOREFR",strStoreFr);
					receiptItemsScnRow.setFieldValue("STORETO",strStoreTo);
					receiptItemsScnRow.setFieldValue("TRANS_NO",strTransNo);
					receiptItemsScnRow.setFieldValue("ITEM_NO",strItemNo);
					
					if(currDate>dueDaysDate)
					{
						receiptItemsScnRow.setStyle("DUE_DATE", redStyle);
						dblDiscountPrice = dblRRP;
					}
					else
					{
						receiptItemsScnRow.setStyle("DUE_DATE", greenStyle);
						//Only apply discount if sales quantity is achieved
						if(moveQty>=dblTargetSalesQty)
						{	
							dblDiscountPrice = dblRRP - (dblRRP * dblDiscountRate);
							
						}
					}
					
					System.out.println("Search - Discount price = "+dblDiscountPrice);
					strDiscountPrice = String.valueOf(dblDiscountPrice);
					System.out.println("Search - Discount price String = "+strDiscountPrice);
//				receiptItemsScnRow.setFieldValue("DISCOUNT_PRICE",dblDiscountPrice);
					receiptItemsScnRow.setFieldValue("DISCOUNT_PRICE",strDiscountPrice);
					
					AllocScrTable.appendRow(receiptItemsScnRow);

				}

			}		

			if(AllocScrTable.getRowCount()>0)
			{
				//Set as current customer
				CurrCust.setFieldValue(strTellyNo);
			}
		}

		if(AllocScrTable.getRowCount()==0)
			return new VtiUserExitResult(000,"No items available from this customer");
		else
			return new VtiUserExitResult(000,"Items retrieved");
	}
}