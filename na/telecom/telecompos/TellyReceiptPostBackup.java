package na.telecom.telecompos;

import java.util.Date;

import au.com.skytechnologies.ecssdk.log.Log;
import au.com.skytechnologies.ecssdk.thread.StableThread;
import au.com.skytechnologies.ecssdk.util.DateFormatter;
import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitLdbOrderSpecification;
import au.com.skytechnologies.vti.VtiExitLdbRequest;
import au.com.skytechnologies.vti.VtiExitLdbSelectCondition;
import au.com.skytechnologies.vti.VtiExitLdbSelectConditionGroup;
import au.com.skytechnologies.vti.VtiExitLdbSelectCriterion;
import au.com.skytechnologies.vti.VtiExitLdbTable;
import au.com.skytechnologies.vti.VtiExitLdbTableRow;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitHeaderInfo;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;
import au.com.skytechnologies.vti.VtiUserExitScreenTableRow;

public class TellyReceiptPostBackup extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Data Declarations.     
		VtiUserExitScreenField OrderNo = getScreenField("ORDER_NO");
		VtiUserExitScreenField PaymentButton = getScreenField("G_PAYMENT");
		VtiUserExitScreenField Deposit = getScreenField("DEP");	  
		VtiUserExitScreenField ExpectedCash = getScreenField("EXP_CASH");
		VtiUserExitScreenField Discount = getScreenField("DISC");
		VtiUserExitScreenField AmountDue = getScreenField("AMOUNT_DUE");
		VtiUserExitScreenField TellyName = getScreenField("TELLY_NAME");
		VtiUserExitScreenField TellyType = getScreenField("TELLTYPE");
		VtiUserExitScreenField CurrCust = getScreenField("CURRENT_CUST");
		VtiUserExitScreenField DueDays = getScreenField("DUE_DAYS");
		VtiUserExitScreenField TellyWalkStore = getScreenField("TELEWALK_STORE");
		VtiUserExitScreenField TellyPointStore = getScreenField("TELEPOINT_STORE");
		VtiUserExitScreenField DefaultPlant = getScreenField("DEF_PLANT");
		VtiUserExitScreenField FromStore = getScreenField("FROM_STORE");
		VtiUserExitScreenField Customer = getScreenField("TELLY_NO");
		VtiUserExitScreenField UserID = getScreenField("USERID");
		VtiUserExitScreenTable AllocScrTable = getScreenTable("ALLOC");
		VtiUserExitScreenField TellyNo = getScreenField("TELLY_NO");
		VtiUserExitScreenField TaxValue = getScreenField("TAX_VALUE");


		VtiExitLdbTable YSPS_TELLY_CUST = getLocalDatabaseTable("YSPS_TELLY_CUST");
		VtiExitLdbTable YSPS_MVMT_HEADER = getLocalDatabaseTable("YSPS_MVMT_HEADER");
		VtiExitLdbTable YSPS_MVMT_ITEMS = getLocalDatabaseTable("YSPS_MVMT_ITEMS");
		VtiExitLdbTable YSPS_TELLY_DEP = getLocalDatabaseTable("YSPS_TELLY_DEP");
		VtiExitLdbTable YSPS_TRAN_QUEUE = getLocalDatabaseTable("YSPS_TRAN_QUEUE");
		VtiExitLdbTable docHeaderLdbTable = getLocalDatabaseTable("YSPS_DOC_HEADER");
		VtiExitLdbTable docItemsLdbTable = getLocalDatabaseTable("YSPS_DOC_ITEMS");
		VtiExitLdbTable YSPS_GEN_CONFIG = getLocalDatabaseTable("YSPS_GENERAL_CONFIGURATION");
		VtiExitLdbTable YSPS_DISCOUNT = getLocalDatabaseTable("YSPS_DISCOUNT");

		if (UserID== null)
			return new VtiUserExitResult(999, "Screen table USERID does not exist");
		if (PaymentButton== null)
			return new VtiUserExitResult(999, "Screen table G_PAYMENT does not exist");
		if (OrderNo== null)
			return new VtiUserExitResult(999, "Screen table ORDER_NO does not exist");
		if (DefaultPlant== null)
			return new VtiUserExitResult(999, "Screen Field DEF_PLANT does not exist");
		if (FromStore== null)
			return new VtiUserExitResult(999, "Screen Field FROM_STORE does not exist");
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
		if (TellyName== null)
			return new VtiUserExitResult(999, "Screen Field TELLY_NAME does not exist");
		if (TellyWalkStore== null)
			return new VtiUserExitResult(999, "Screen Field TELEWALK_STORE does not exist");
		if (TellyPointStore== null)
			return new VtiUserExitResult(999, "Screen Field TELEPOINT_STORE does not exist");
		if (TellyType== null)
			return new VtiUserExitResult(999, "Screen Field TELLTYPE does not exist");
		if (AllocScrTable== null)
			return new VtiUserExitResult(999, "Screen table ALLOC does not exist");
		if (YSPS_TELLY_CUST== null)
			return new VtiUserExitResult(999, "LDB table YSPS_TELLY_CUST does not exist");
		if (YSPS_TELLY_DEP== null)
			return new VtiUserExitResult(999, "LDB table YSPS_TELLY_DEP does not exist");
		if (YSPS_MVMT_HEADER== null)
			return new VtiUserExitResult(999, "LDB table YSPS_MVMT_HEADER does not exist");
		if (YSPS_MVMT_ITEMS== null)
			return new VtiUserExitResult(999, "LDB table YSPS_MVMT_ITEMS does not exist");
		if (Customer== null)
			return new VtiUserExitResult(999, "Screen table TELLY_NO does not exist");
		if (YSPS_TRAN_QUEUE== null)
			return new VtiUserExitResult(999, "LDB table YSPS_TRAN_QUEUE does not exist");
		if (docHeaderLdbTable == null)
			return new VtiUserExitResult(999, "LDB table YSPS_DOC_HEADER not found");
		if (docItemsLdbTable == null)
			return new VtiUserExitResult(999, "LDB table YSPS_DOC_ITEMS not found");
		if (YSPS_GEN_CONFIG== null)
			return new VtiUserExitResult(999, "LDB table YSPS_GENERAL_CONFIGURATION does not exist");
		if (YSPS_DISCOUNT== null)
			return new VtiUserExitResult(999, "LDB table YSPS_DISCOUNT does not exist");
		if (TellyNo== null)
			return new VtiUserExitResult(999, "Screen Field TELLY_NO does not exist");
		if (TaxValue== null)
			return new VtiUserExitResult(999, "Screen Field TAX_VALUE does not exist");


		String strReturnTranType 	= "TELLY RECEIPT";
		String strDiscount          = "DISCOUNT";
		String strTellyNo = TellyNo.getFieldValue();

		Date currNow = new Date();
		int currDate = Integer.parseInt(DateFormatter.format("yyyyMMdd", currNow));
		int currTime = Integer.parseInt(DateFormatter.format("HHmmss", currNow));

		double tax = 0; //0.10;
		double orderTotal = 0;
		double orderTax = 0;
		String strPriceCond = new String();

		boolean blnDataVerified = false;
		boolean blnSalesTrans = false;
		boolean blnReturnTrans = false;
		String strServerGroup = getServerGroup();
		String strServerID = getServerId();
		String strTellyType = TellyType.getFieldValue();
		String strTellyWalkStore 	= TellyWalkStore.getFieldValue();
		String strTellyPointStore 	= TellyPointStore.getFieldValue();
		String strDefaultPlant		= DefaultPlant.getFieldValue();
		String strSaleableStore      = FromStore.getFieldValue();
		long salesTranNo = 0;
		long returnTranNo = 0;
		String strCustomer			= Customer.getFieldValue();
		String strUserID			= UserID.getFieldValue();
		String strCurrTm = new String();
		int actualQtyCount =0;
		int  intSalesItemNo = 0;
		int  intReturnItemNo = 0;
		double dblTotalAlloc=0;
		double dblTotalValueReturned = 0;

		if(currTime<100000)
		{
			strCurrTm = String.valueOf(currTime);
			actualQtyCount = strCurrTm.length();

			while (actualQtyCount <6)
			{
				strCurrTm = "0" + strCurrTm ;
				actualQtyCount = strCurrTm.length();
			}


		}
		else
		{
			strCurrTm = String.valueOf(currTime);
		}

		if(AllocScrTable.getRowCount()==0)
			return new VtiUserExitResult(999,"There are no receipts to POST");

		//Check if any data to post
		for(int a=0;a<AllocScrTable.getRowCount();a++)
		{
			VtiUserExitScreenTableRow allocScrTableRow = AllocScrTable.getRow(a);
			String strProcessed = allocScrTableRow.getFieldValue("PROCESSED");
			if(strProcessed.equals("X"))
			{
				blnDataVerified = true;
				a=AllocScrTable.getRowCount(); //End loop
			}
		}

		if( blnDataVerified==false)
			return new VtiUserExitResult(999,"There are no receipts to POST");



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

		tax = taxCondLDBRow[0].getDoubleFieldValue("ATTRIBUTE1");
		orderTotal = AmountDue.getDoubleFieldValue();
		orderTax   = TaxValue.getDoubleFieldValue();

		//Loop throught the receipt table and retrieve data for posting
		for(int b=0;b<AllocScrTable.getRowCount();b++)
		{
			VtiUserExitScreenTableRow allocScrTableRow = AllocScrTable.getRow(b);

			String moveMatnr = allocScrTableRow.getFieldValue("ITEM");
			String moveMatnrDesc = allocScrTableRow.getFieldValue("DESC");
			double dblSoldQty = allocScrTableRow.getDoubleFieldValue("SOLD");
			double dblReturnQty = allocScrTableRow.getDoubleFieldValue("RETURN");

			double dblRRP = allocScrTableRow.getDoubleFieldValue("RRP");
			double dblDiscountPrice = allocScrTableRow.getDoubleFieldValue("DISCOUNT_PRICE");
			double dblTotalSold= dblSoldQty * dblDiscountPrice;
			double dblTotalReturn= dblReturnQty * dblRRP;
			double dblTotalSoldTaxAmt = dblTotalSold * tax; 
			
			dblTotalValueReturned+= dblTotalReturn + (dblSoldQty *dblRRP);

			String strHistTranNo = allocScrTableRow.getFieldValue("TRANS_NO");
			String strHistItemNo= allocScrTableRow.getFieldValue("ITEM_NO");
			String strCustomerStore = new String();

			if(strTellyType.equals("TELLY WALKER"))
				strCustomerStore =strTellyWalkStore;
			else if(strTellyType.equals("TELLY POINT"))
				strCustomerStore =strTellyPointStore;

			//Create movement items for Sales transaction
			if(dblSoldQty>0)
			{
				if(salesTranNo==0)
				{
					salesTranNo = getNextNumberFromNumberRange("YSPS_ORDER");

				}

				intSalesItemNo += 1;


				//Update the history
				VtiExitLdbSelectCriterion[] historySelCond =
				{
						new VtiExitLdbSelectCondition("SERVERGROUP",
								VtiExitLdbSelectCondition.EQ_OPERATOR, strServerGroup),
								new VtiExitLdbSelectCondition("SERVERID",
										VtiExitLdbSelectCondition.EQ_OPERATOR, strServerID),
										new VtiExitLdbSelectCondition("TRANS_NO",
												VtiExitLdbSelectCondition.EQ_OPERATOR,strHistTranNo),
												new VtiExitLdbSelectCondition("ITEM_NO",
														VtiExitLdbSelectCondition.EQ_OPERATOR,strHistItemNo),


				};

				VtiExitLdbSelectConditionGroup historySelCondGrp =
					new VtiExitLdbSelectConditionGroup(historySelCond, true);


				VtiExitLdbTableRow[] historyLDBRows =
					YSPS_MVMT_ITEMS.getMatchingRows(historySelCondGrp);

				for(int d=0; d<historyLDBRows.length; d++)
				{
					double dblIssueQty = historyLDBRows[d].getDoubleFieldValue("MENGE");
					double dblReceiveQty = historyLDBRows[d].getDoubleFieldValue("RECEIPT_QTY");
					double dblOpenQty = 0;

					dblOpenQty = (dblIssueQty - dblReceiveQty) - dblSoldQty;

					historyLDBRows[d].setFieldValue("RECEIPT_QTY", dblOpenQty);
					if(dblOpenQty==0)
					{
						historyLDBRows[d].setFieldValue("FINAL_RECEIPT", "X");
					}
					historyLDBRows[d].setFieldValue("TIMESTAMP", "");
					YSPS_MVMT_ITEMS.saveRow(historyLDBRows[d]);

				}



				blnSalesTrans = true;
				VtiExitLdbTableRow newItemRow = docItemsLdbTable.newRow();

				newItemRow.setFieldValue("VTI_REF", salesTranNo); 
				newItemRow.setFieldValue("SERVERID", strServerID);
				newItemRow.setFieldValue("ITEM_NO", intSalesItemNo);
				newItemRow.setFieldValue("MATERIAL", moveMatnr);
				newItemRow.setFieldValue("MAT_DESC", moveMatnrDesc);
				newItemRow.setFieldValue("EAN", "");
				newItemRow.setDoubleFieldValue("RR_PRICE", dblRRP);
				newItemRow.setDoubleFieldValue("ACT_PRICE", dblDiscountPrice);
				newItemRow.setFieldValue("ITEM_QTY", dblSoldQty);

				double taxPrice = dblRRP * (1 + tax);

				newItemRow.setDoubleFieldValue("RRP_INCLTAX", taxPrice);
				taxPrice = dblDiscountPrice * (1 + tax);

				newItemRow.setDoubleFieldValue("ACTPRICE_INCTAX", taxPrice);
				newItemRow.setDoubleFieldValue("ITEM_TOTAL", dblSoldQty*taxPrice);
				newItemRow.setFieldValue("STORE", strCustomerStore);
				newItemRow.setFieldValue("PLANT", strDefaultPlant);
				newItemRow.setFieldValue("TIMESTAMP", "INPROGRESS");


				try
				{
					docItemsLdbTable.saveRow(newItemRow);
				}
				catch(VtiExitException ee)
				{
					Log.error("Error Saving New Item LDB Row", ee);
					return new VtiUserExitResult(999, "Error Updating New Item LDB Row");
				}

			}

			//Create movement items for Return transaction
			if(dblReturnQty>0)
			{
				blnReturnTrans = true;

				if(returnTranNo==0)
				{
					returnTranNo = getNextNumberFromNumberRange("YSPS_ORDER");
				}

				intReturnItemNo+= 1;

				//Update the history
				VtiExitLdbSelectCriterion[] historySelCond =
				{
						new VtiExitLdbSelectCondition("SERVERGROUP",
								VtiExitLdbSelectCondition.EQ_OPERATOR, strServerGroup),
								new VtiExitLdbSelectCondition("SERVERID",
										VtiExitLdbSelectCondition.EQ_OPERATOR, strServerID),
										new VtiExitLdbSelectCondition("TRANS_NO",
												VtiExitLdbSelectCondition.EQ_OPERATOR,strHistTranNo),
												new VtiExitLdbSelectCondition("ITEM_NO",
														VtiExitLdbSelectCondition.EQ_OPERATOR,strHistItemNo),


				};

				VtiExitLdbSelectConditionGroup historySelCondGrp =
					new VtiExitLdbSelectConditionGroup(historySelCond, true);


				VtiExitLdbTableRow[] historyLDBRows =
					YSPS_MVMT_ITEMS.getMatchingRows(historySelCondGrp);

				for(int d=0; d<historyLDBRows.length; d++)
				{
					double dblIssueQty = historyLDBRows[d].getDoubleFieldValue("MENGE");
					double dblReceiveQty = historyLDBRows[d].getDoubleFieldValue("RECEIPT_QTY");
					double dblOpenQty = 0;

					dblOpenQty = (dblIssueQty - dblReceiveQty) - dblReturnQty;

					historyLDBRows[d].setFieldValue("RECEIPT_QTY", dblOpenQty);
					if(dblOpenQty==0)
					{
						historyLDBRows[d].setFieldValue("FINAL_RECEIPT", "X");
					}
					historyLDBRows[d].setFieldValue("TIMESTAMP", "");
					YSPS_MVMT_ITEMS.saveRow(historyLDBRows[d]);

				}

				VtiExitLdbTableRow moveItemLdbRows = YSPS_MVMT_ITEMS.newRow();

				moveItemLdbRows.setFieldValue("SERVERGROUP",strServerGroup);
				moveItemLdbRows.setFieldValue("SERVERID",strServerID);
				moveItemLdbRows.setFieldValue("TRANS_NO",returnTranNo);
				moveItemLdbRows.setFieldValue("ITEM_NO",intReturnItemNo);
				moveItemLdbRows.setFieldValue("MATNR",moveMatnr);
				moveItemLdbRows.setFieldValue("MAT_DESC",moveMatnrDesc);
				moveItemLdbRows.setFieldValue("MENGE",dblReturnQty);
				moveItemLdbRows.setFieldValue("DMBTR",dblTotalReturn);
				moveItemLdbRows.setFieldValue("RECEIPT_QTY",dblReturnQty);
				moveItemLdbRows.setFieldValue("FINAL_RECEIPT","X");
				moveItemLdbRows.setFieldValue("PLANTFR",strDefaultPlant);
				moveItemLdbRows.setFieldValue("STORETO",strSaleableStore);
				moveItemLdbRows.setFieldValue("STOREFR",strCustomerStore);

				moveItemLdbRows.setFieldValue("CRE_DATE",currDate);

				YSPS_MVMT_ITEMS.saveRow(moveItemLdbRows);

			}
		}


		//Create movement header and tran Q for Sales
		if(blnSalesTrans==true)
		{

			OrderNo.setFieldValue(salesTranNo);
			//Unhide payment button
			PaymentButton.setFieldValue("TN_PAYMENT");

			//Create sales movement header
			VtiExitLdbTableRow headerLdbRow = docHeaderLdbTable.newRow();
			headerLdbRow.setFieldValue("DOC_TYPE", "TELLY ORDER");
			headerLdbRow.setFieldValue("VTI_REF", salesTranNo);
			headerLdbRow.setFieldValue("SERVERID", strServerID);
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


		}

		//Create movement header and tran Q for Returns
		if(blnReturnTrans==true)
		{
			//Create movement header
			VtiExitLdbTableRow moveHeaderLdbRow = YSPS_MVMT_HEADER.newRow();

			moveHeaderLdbRow.setFieldValue("SERVERGROUP",strServerGroup);
			moveHeaderLdbRow.setFieldValue("SERVERID",strServerID);
			moveHeaderLdbRow.setFieldValue("TRANS_NO",returnTranNo);
			moveHeaderLdbRow.setFieldValue("TRAN_TYPE",strReturnTranType);
			moveHeaderLdbRow.setFieldValue("KUNNR",strCustomer);
			moveHeaderLdbRow.setFieldValue("USERID",strUserID);
			moveHeaderLdbRow.setFieldValue("CRE_DATE",currDate);
			moveHeaderLdbRow.setFieldValue("CRE_TIME",strCurrTm);

			YSPS_MVMT_HEADER.saveRow(moveHeaderLdbRow);

			long lgTranQNo = getNextNumberFromNumberRange("YSPS_TRAN_NO");

			//create Tran Q record
			VtiExitLdbTableRow tranQueueLDBRow = YSPS_TRAN_QUEUE.newRow();

			tranQueueLDBRow.setFieldValue("SERVERID",strServerID);
			tranQueueLDBRow.setFieldValue("TRAN_NUMBER",lgTranQNo);
			tranQueueLDBRow.setFieldValue("TRAN_TYPE",strReturnTranType);
			tranQueueLDBRow.setFieldValue("VTI_REF",returnTranNo);
			tranQueueLDBRow.setFieldValue("NO_ITEMS",intReturnItemNo);
			tranQueueLDBRow.setFieldValue("SERVER_GROUP",strServerGroup);

			YSPS_TRAN_QUEUE.saveRow(tranQueueLDBRow);


		}


		//Update the deposit table
		VtiExitLdbSelectCriterion[] depositSelCond =
		{
				new VtiExitLdbSelectCondition("SERVER_GROUP",
						VtiExitLdbSelectCondition.EQ_OPERATOR, strServerGroup),
						new VtiExitLdbSelectCondition("KUNNR",
								VtiExitLdbSelectCondition.EQ_OPERATOR, strCustomer),
								new VtiExitLdbSelectCondition("DEL_IND",
										VtiExitLdbSelectCondition.NE_OPERATOR, "X"),
		};

		VtiExitLdbSelectConditionGroup depositSelCondGrp =
			new VtiExitLdbSelectConditionGroup(depositSelCond, true);


		VtiExitLdbTableRow[] depositLdbRows =
			YSPS_TELLY_DEP.getMatchingRows(depositSelCondGrp);

		//if record exists, modify value
		for(int a=0; a<depositLdbRows.length; a++)
		{
			//Assign total allocated
			dblTotalAlloc = depositLdbRows[a].getDoubleFieldValue("AMOUNT_ALLOCATED");
			dblTotalAlloc -= dblTotalValueReturned;

			//If amount allocated is negative, default to zero
			if(dblTotalAlloc<0)
				dblTotalAlloc = 0;

			depositLdbRows[a].setDoubleFieldValue("AMOUNT_ALLOCATED", dblTotalAlloc);

			depositLdbRows[a].setFieldValue("TIMESTAMP", "");
			YSPS_TELLY_DEP.saveRow(depositLdbRows[a]);
		}

		//Clear fields
		Deposit.setFieldValue("");	  
		Discount.setFieldValue("");
		AmountDue.setFieldValue("");
		ExpectedCash.setFieldValue("");
		TellyName.setFieldValue("");
		Customer.setFieldValue("");
		AllocScrTable.clear();
		CurrCust.setFieldValue("");
		setCursorPosition(Customer);

		// Trigger the uploads to SAP, if a connection is available.
		String hostName = getHostInterfaceName();
		boolean hostConnected = isHostInterfaceConnected(hostName);

		if (hostConnected)
		{ 
			VtiExitLdbRequest ldbReqUploadMvmtHeader = 
				new VtiExitLdbRequest(YSPS_MVMT_HEADER,VtiExitLdbRequest.UPLOAD);
			VtiExitLdbRequest ldbReqUploadMvmtItems = 
				new VtiExitLdbRequest(YSPS_MVMT_ITEMS,VtiExitLdbRequest.UPLOAD);
			VtiExitLdbRequest ldbReqUploadTellyDep = 
				new VtiExitLdbRequest(YSPS_TELLY_DEP,VtiExitLdbRequest.UPLOAD);
			VtiExitLdbRequest ldbReqUploadTranQueue = 
				new VtiExitLdbRequest(YSPS_TRAN_QUEUE,VtiExitLdbRequest.UPLOAD);

			ldbReqUploadMvmtHeader.submit(false);
			ldbReqUploadMvmtItems.submit(false);
			ldbReqUploadTellyDep.submit(false);
			StableThread.snoozeCurrentThread(2000);
			ldbReqUploadTranQueue.submit(false);

		}

		//Open the payment screen
		if(blnSalesTrans==true)
		{		
			VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
			sessionHeader.setNextFunctionId("YSPS_PAYMENT");			
		}

		return new VtiUserExitResult(000,"Items Posted");

	}
}