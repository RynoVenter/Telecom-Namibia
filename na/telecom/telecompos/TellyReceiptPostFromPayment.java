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

public class TellyReceiptPostFromPayment
{
	public static void Process(VtiUserExit l_exit, String l_Customer) throws VtiExitException
	{
		// Data Declarations.     


		VtiExitLdbTable YSPS_MVMT_HEADER = l_exit.getLocalDatabaseTable("YSPS_MVMT_HEADER");
		VtiExitLdbTable YSPS_MVMT_ITEMS = l_exit.getLocalDatabaseTable("YSPS_MVMT_ITEMS");
		VtiExitLdbTable YSPS_TELLY_DEP = l_exit.getLocalDatabaseTable("YSPS_TELLY_DEP");
		VtiExitLdbTable YSPS_TRAN_QUEUE = l_exit.getLocalDatabaseTable("YSPS_TRAN_QUEUE");
		VtiExitLdbTable docHeaderLdbTable = l_exit.getLocalDatabaseTable("YSPS_DOC_HEADER");
		VtiExitLdbTable docItemsLdbTable = l_exit.getLocalDatabaseTable("YSPS_DOC_ITEMS");
		VtiExitLdbTable YSPS_GEN_CONFIG = l_exit.getLocalDatabaseTable("YSPS_GENERAL_CONFIGURATION");
		VtiExitLdbTable YSPS_DISCOUNT = l_exit.getLocalDatabaseTable("YSPS_DISCOUNT");
		VtiExitLdbTable YSPS_TELLY_RECEIPT_WORKING = l_exit.getLocalDatabaseTable("YSPS_TELLY_RECEIPT_WORKING");
		//VtiUserExitScreenField UserID = l_exit.getScreenField("USERID");

		if (YSPS_MVMT_HEADER== null)
			throw new VtiExitException("LDB table YSPS_MVMT_HEADER does not exist");
		if (YSPS_MVMT_ITEMS== null)
			throw new VtiExitException("LDB table YSPS_MVMT_ITEMS does not exist");
		if (YSPS_TRAN_QUEUE== null)
			throw new VtiExitException("LDB table YSPS_TRAN_QUEUE does not exist");
		if (docHeaderLdbTable == null)
			throw new VtiExitException("LDB table YSPS_DOC_HEADER does not exist");
		if (docItemsLdbTable == null)
			throw new VtiExitException("LDB table YSPS_DOC_ITEMS does not exist");
		if (YSPS_GEN_CONFIG== null)
			throw new VtiExitException("LDB table YSPS_GENERAL_CONFIGURATION does not exist");
		if (YSPS_DISCOUNT== null)
			throw new VtiExitException("LDB table YSPS_DISCOUNT does not exist");
		if (YSPS_TELLY_RECEIPT_WORKING== null)
			throw new VtiExitException("LDB table YSPS_TELLY_RECEIPT_WORKING does not exist");
		/*if (UserID== null)
			throw new VtiExitException("Screen field UserID does not exist");
*/
		String strReturnTranType 	= "TELLY RECEIPT";
		String strServerID = l_exit.getServerId();
		String strServerGroup = l_exit.getServerGroup();
		long returnTranNo = 0;
		int intReturnItemNo = 0;
		Date currNow = new Date();
		String currDate = DateFormatter.format("yyyyMMdd", currNow);
		String currTime = DateFormatter.format("HHmmss", currNow);
		//String strUserID = UserID.getFieldValue();
		String strUserID = new String();
		double dblTotalValueReturned = 0;
		double dblTotalReturn= 0;
		double dblTotalSales =0;
		
		boolean blnReturn = false;
		double tax = 0;
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
			throw new VtiExitException("Sales VAT value not found in general configuration");

		tax = taxCondLDBRow[0].getDoubleFieldValue("ATTRIBUTE1");

		//search from working table
		VtiExitLdbSelectCriterion[] workingSelCond =
		{
				new VtiExitLdbSelectCondition("KUNNR",
						VtiExitLdbSelectCondition.EQ_OPERATOR, l_Customer),

		};

		VtiExitLdbSelectConditionGroup workingSelCondGrp =
			new VtiExitLdbSelectConditionGroup(workingSelCond, true);


		VtiExitLdbTableRow[] workingLDBRows =
			YSPS_TELLY_RECEIPT_WORKING.getMatchingRows(workingSelCondGrp);

		if(workingLDBRows.length==0)
			throw new VtiExitException("No telly receipt records to process");

		
		System.out.println("Performing telly receipt table updates");
		for(int a=0; a<workingLDBRows.length; a++)
		{
			String strTranNo = workingLDBRows[a].getFieldValue("TRAN_NO");
			String strItemNo = workingLDBRows[a].getFieldValue("ITEM_NO");
			double dblSold = workingLDBRows[a].getDoubleFieldValue("SOLD");
			double dblReturn = workingLDBRows[a].getDoubleFieldValue("RETURN");
			String strMatnr  = workingLDBRows[a].getFieldValue("MATNR");
			String strMatnrDesc  = workingLDBRows[a].getFieldValue("DESCRIPTION");
			double dblRRP = workingLDBRows[a].getDoubleFieldValue("RRP");
			double dblDiscountPrice = workingLDBRows[a].getDoubleFieldValue("DISCOUNT_PRICE");
			String strPlantFr = workingLDBRows[a].getFieldValue("PLANT_FR");
			String strStoreFr = workingLDBRows[a].getFieldValue("STORE_FR");
			String strStoreTo = workingLDBRows[a].getFieldValue("STORE_TO");
			strUserID =  workingLDBRows[a].getFieldValue("USERID");
			
			//double dblTotalReturn = 0;

			//Update the movement item table
			VtiExitLdbSelectCriterion[] moveItemsSelCond =
			{
					new VtiExitLdbSelectCondition("SERVERGROUP",
							VtiExitLdbSelectCondition.EQ_OPERATOR,strServerGroup),
							new VtiExitLdbSelectCondition("SERVERID",
									VtiExitLdbSelectCondition.EQ_OPERATOR, strServerID),
									new VtiExitLdbSelectCondition("TRANS_NO",
											VtiExitLdbSelectCondition.EQ_OPERATOR, strTranNo),
											new VtiExitLdbSelectCondition("ITEM_NO",
													VtiExitLdbSelectCondition.EQ_OPERATOR, strItemNo),


			};

			VtiExitLdbSelectConditionGroup moveItemsSelCondGrp =
				new VtiExitLdbSelectConditionGroup(moveItemsSelCond, true);


			VtiExitLdbTableRow[] moveItemsLDBRows =
				YSPS_MVMT_ITEMS.getMatchingRows(moveItemsSelCondGrp);

			for(int d=0; d<moveItemsLDBRows.length; d++)
			{
				double dblIssueQty = moveItemsLDBRows[d].getDoubleFieldValue("MENGE");
				double dblReceiveQty = moveItemsLDBRows[d].getDoubleFieldValue("RECEIPT_QTY");
				double dblOpenQty = 0;

				dblOpenQty = (dblIssueQty - dblReceiveQty) - (dblSold+dblReturn);

				moveItemsLDBRows[d].setFieldValue("RECEIPT_QTY", dblOpenQty);

				if(dblOpenQty==0)
				{
					moveItemsLDBRows[d].setFieldValue("FINAL_RECEIPT", "X");
				}

				moveItemsLDBRows[d].setFieldValue("TIMESTAMP", "");
				YSPS_MVMT_ITEMS.saveRow(moveItemsLDBRows[d]);

			}
			
			dblTotalReturn = 0;
			dblTotalSales = 0;
			
			if(dblSold>0)
			{	
				dblTotalSales = dblSold * dblRRP;
			}
			
			//Create returns transaction items
			if(dblReturn>0)
			{
				if(returnTranNo==0)
				{
					returnTranNo = l_exit.getNextNumberFromNumberRange("YSPS_ORDER");
				}
				intReturnItemNo+= 1;
				
				System.out.println("Creating receipt record item " + intReturnItemNo);
				blnReturn = true;


				VtiExitLdbTableRow moveItemLdbRows = YSPS_MVMT_ITEMS.newRow();

				moveItemLdbRows.setFieldValue("SERVERGROUP",strServerGroup);
				moveItemLdbRows.setFieldValue("SERVERID",strServerID);
				moveItemLdbRows.setFieldValue("TRANS_NO",returnTranNo);
				moveItemLdbRows.setFieldValue("ITEM_NO",intReturnItemNo);
				moveItemLdbRows.setFieldValue("MATNR",strMatnr);
				moveItemLdbRows.setFieldValue("MAT_DESC",strMatnrDesc);
				moveItemLdbRows.setFieldValue("MENGE",dblReturn);

				//Total return value
				dblTotalReturn = dblReturn * dblRRP;

				moveItemLdbRows.setFieldValue("DMBTR",dblTotalReturn);
				moveItemLdbRows.setFieldValue("RECEIPT_QTY",dblReturn);
				moveItemLdbRows.setFieldValue("FINAL_RECEIPT","X");
				moveItemLdbRows.setFieldValue("PLANTFR",strPlantFr);
				moveItemLdbRows.setFieldValue("STORETO",strStoreTo);
				moveItemLdbRows.setFieldValue("STOREFR",strStoreFr);

				moveItemLdbRows.setFieldValue("CRE_DATE",currDate);
				moveItemLdbRows.setFieldValue("TIMESTAMP","");
				moveItemLdbRows.setFieldValue("DEL_IND","");

				YSPS_MVMT_ITEMS.saveRow(moveItemLdbRows);

				
			} //end for return
			
			dblTotalValueReturned+= dblTotalReturn + dblTotalSales;
			
			
		}//endfor
		
		
		//Update the deposit table
		VtiExitLdbSelectCriterion[] depositSelCond =
		{
				new VtiExitLdbSelectCondition("SERVER_GROUP",
						VtiExitLdbSelectCondition.EQ_OPERATOR, strServerGroup),
						new VtiExitLdbSelectCondition("KUNNR",
								VtiExitLdbSelectCondition.EQ_OPERATOR, l_Customer),
								new VtiExitLdbSelectCondition("DEL_IND",
										VtiExitLdbSelectCondition.NE_OPERATOR, "X"),
		};

		VtiExitLdbSelectConditionGroup depositSelCondGrp =
			new VtiExitLdbSelectConditionGroup(depositSelCond, true);


		VtiExitLdbTableRow[] depositLdbRows =
			YSPS_TELLY_DEP.getMatchingRows(depositSelCondGrp);

		System.out.println("Updating deposit table");
		//if record exists, modify value
		for(int a=0; a<depositLdbRows.length; a++)
		{
			double dblTotalAlloc = 0;
			
			//Assign total allocated
			dblTotalAlloc = depositLdbRows[a].getDoubleFieldValue("AMOUNT_ALLOCATED");
			dblTotalAlloc -= dblTotalValueReturned;

			//If amount allocated is negative, default to zero
			if(dblTotalAlloc<0)
				dblTotalAlloc = 0;

			depositLdbRows[a].setDoubleFieldValue("AMOUNT_ALLOCATED", dblTotalAlloc);

			depositLdbRows[a].setFieldValue("TIMESTAMP", "");
			YSPS_TELLY_DEP.saveRow(depositLdbRows[a]);
			System.out.println("Deposit table updated for customer "+ l_Customer);
		}


		if(blnReturn)
		{

			System.out.println("Creating movement header record");
			
			//Create a return transaction 

			//Header transaction record
			VtiExitLdbTableRow moveHeaderLdbRow = YSPS_MVMT_HEADER.newRow();

			moveHeaderLdbRow.setFieldValue("SERVERGROUP",strServerGroup);
			moveHeaderLdbRow.setFieldValue("SERVERID",strServerID);
			moveHeaderLdbRow.setFieldValue("TRANS_NO",returnTranNo);
			moveHeaderLdbRow.setFieldValue("TRAN_TYPE",strReturnTranType);
			moveHeaderLdbRow.setFieldValue("KUNNR",l_Customer);
			moveHeaderLdbRow.setFieldValue("USERID",strUserID);
			moveHeaderLdbRow.setFieldValue("CRE_DATE",currDate);
			moveHeaderLdbRow.setFieldValue("CRE_TIME",currTime);

			YSPS_MVMT_HEADER.saveRow(moveHeaderLdbRow);

			long lgTranQNo = l_exit.getNextNumberFromNumberRange("YSPS_TRAN_NO");

			System.out.println("Creating transaction Q record");
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
		
		String hostName = l_exit.getHostInterfaceName();
		boolean hostConnected = l_exit.isHostInterfaceConnected(hostName);

		if (hostConnected)
		{ 
			System.out.println("Kicking off table update back to SAP");

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
		
	}
}