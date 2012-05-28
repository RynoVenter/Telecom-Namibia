package na.telecom.telecompos;

import java.util.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class PrintSmallDocket
{
  public final static int SALE = 0;
  public final static int RETURN = 1;
  public final static int PRE_SALE = 2;

  public static void print(VtiUserExit vtiUsrExt, String vtiRefNo, int docType,
                           Hashtable paymentDetails, int deviceId) throws VtiExitException
  {
    // LDB Definition

    VtiExitLdbTable paymentLdbTable =
        vtiUsrExt.getLocalDatabaseTable("YSPS_PAYMENT_TRANSACTION");
    VtiExitLdbTable docHeaderLdbTable =
        vtiUsrExt.getLocalDatabaseTable("YSPS_DOC_HEADER");
    VtiExitLdbTable documentItemLdbTable =
        vtiUsrExt.getLocalDatabaseTable("YSPS_DOC_ITEMS");
    VtiExitLdbTable docTextLdbTable =
        vtiUsrExt.getLocalDatabaseTable("YSPS_DOC_TEXT");
    VtiExitLdbTable logonLdbTable =
        vtiUsrExt.getLocalDatabaseTable("YSPS_LOGON");
    VtiExitLdbTable customerLdbTable =
        vtiUsrExt.getLocalDatabaseTable("YSPS_CUSTOMER");

    if (paymentLdbTable == null)
    {
      throw new VtiExitException("LDB table YSPS_PAYMENT_TRANSACTION not found");
    }
    if (docHeaderLdbTable == null)
    {
      throw new VtiExitException("LDB table YSPS_DOC_HEADER not found");
    }
    if (documentItemLdbTable == null)
    {
      throw new VtiExitException("LDB table YSPS_DOC_ITEMS not found");
    }
    if (docTextLdbTable == null)
    {
      throw new VtiExitException("LDB table YSPS_DOC_TEXT not found");
    }
    if (logonLdbTable == null)
    {
      throw new VtiExitException("LDB table YSPS_LOGON not found");
    }
    if (customerLdbTable == null)
    {
      throw new VtiExitException("LDB table YSPS_CUSTOMER not found");
    }

    String vtiServerId = vtiUsrExt.getServerId();
    String userId = "";
    String firstName = "";
    String lastName = "";
    String name = "";
    String lineSeparator = System.getProperty("line.separator");
    String spaces = "            ";
    String spaces7 = "       ";
    String spaces35 = "                                   ";
    String spaces30 = "                              ";
    String spaces15 = "               ";
    String spaces20 = "                    ";
    String itemNo = "";
    String docText = "";
    String docText2 = "";
    String receiptTotal = "";
    String stRrPrice = "";
    String stActPrice = "";
    String stDiscount = "";
    String orderGst = "";
    String currDateP = DateFormatter.format("dd/MM/yyyy");
    String currTimeP = DateFormatter.format("HH:mm:ss");

    String itemTotalPrice = "";
    double discount = 0;
    double rrPrice = 0;
    double actPrice = 0;
    String orderTotal = "";

    int lineQty = 0;

    StringBuffer text = new StringBuffer();
    StringBuffer endText = new StringBuffer();
//?????? 4 print
    double payAmount = ( (Double) paymentDetails.get("PAY_AMOUNT")).doubleValue();
    String stPayAmount = StringUtil.doubleToString(payAmount, 2);
    double totalChange = ( (Double) paymentDetails.get("TOTAL_CHANGE")).doubleValue();
    double totalOwing = ( (Double) paymentDetails.get("TOTAL_OWING")).doubleValue();
    totalOwing = ( (double) Math.round(totalOwing * 100)) / 100;
    String paymentType = (String) paymentDetails.get("PAYMENT_TYPE");
    paymentType = StringUtil.stringPad(paymentType, 15);

    // Set Variable for the report, based on the type.
    if (docType > 2)
    {
      throw new VtiExitException("Unknown Document Type");
    }

    if (deviceId > 3)
    {
      throw new VtiExitException("Maximum of 3 POS lanes allowed for Printing");
    }

    if (deviceId == 0)
    {
      throw new VtiExitException("No POS Lane Entered for Printing");
    }

    // Select the Document Header
    VtiExitLdbSelectCriterion docHeaderSelConds =
        new VtiExitLdbSelectCondition("VTI_REF",
                                      VtiExitLdbSelectCondition.EQ_OPERATOR, vtiRefNo);

    VtiExitLdbTableRow[] docHeaderLdbRows =
        docHeaderLdbTable.getMatchingRows(docHeaderSelConds);

    if (docHeaderLdbRows.length == 0)
    {
      throw new VtiExitException("Failed to Select Doc Header");
    }

    // get userid, and name.
    userId = docHeaderLdbRows[0].getFieldValue("USERID");

    VtiExitLdbSelectCriterion logonSelConds =
        new VtiExitLdbSelectCondition("USERID",
                                      VtiExitLdbSelectCondition.EQ_OPERATOR, userId);

    VtiExitLdbTableRow[] logonLdbRows =
        logonLdbTable.getMatchingRows(logonSelConds);

    // Check that the user was found was found.  If so set the first and last name

    if (logonLdbRows.length == 0)
    {
      throw new VtiExitException("Invalid UserId on Doc Header");
    }

    firstName = logonLdbRows[0].getFieldValue("FIRST_NAME");
    lastName = logonLdbRows[0].getFieldValue("LAST_NAME");
    name = firstName + " " + lastName;

    // Build Selection Criteria to select all items for the doc item LDB.
    VtiExitLdbSelectCriterion[] itemSelConds =
        {
        new VtiExitLdbSelectCondition("VTI_REF",
                                      VtiExitLdbSelectCondition.EQ_OPERATOR, vtiRefNo),
        new VtiExitLdbSelectCondition("SERVERID",
                                      VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId),
        new VtiExitLdbSelectCondition("ACT_PRICE",
                                      VtiExitLdbSelectCondition.GT_OPERATOR, "0")
    };

    VtiExitLdbSelectConditionGroup itemSelCondGrp =
        new VtiExitLdbSelectConditionGroup(itemSelConds, true);

    VtiExitLdbOrderSpecification itemNoOrder =
        new VtiExitLdbOrderSpecification("ITEM_NO");

    VtiExitLdbTableRow[] itemLdbRows =
        documentItemLdbTable.getMatchingRows(itemSelCondGrp, itemNoOrder);

    // Check that some rows are returned.  If not stop.
    if (itemLdbRows.length == 0)
    {
      throw new VtiExitException("No Items For Order");
    }

    //orderTotal = ((double)Math.round(orderTotal * 100)) / 100;
    //receiptTotal = StringUtil.doubleToString(orderTotal, 2);

    receiptTotal = docHeaderLdbRows[0].getFieldValue("ORDER_TOTAL");

    if (docType == PRE_SALE)
    {
      docText = "This is a Pre-Sale Order.  Goods have not";
      docText2 = "been received.";
    }
    else
    {
      docText = "";
      docText2 = "";
    }

    // Loop through the items and build up the docket details.
    for (int i = 0; i < itemLdbRows.length; ++i)
    {
      String matdesc = itemLdbRows[i].getFieldValue("MAT_DESC");
      String material = itemLdbRows[i].getFieldValue("MATERIAL");

      // get document item with the GST prices, and Total Item Value.
      //rrGstPrice = itemLdbRows[i].getDoubleFieldValue("RRP_INCLGST");
      //actGstPrice = itemLdbRows[i].getDoubleFieldValue("ACTPRICE_INCGST");
      rrPrice = itemLdbRows[i].getDoubleFieldValue("RR_PRICE");
      actPrice = itemLdbRows[i].getDoubleFieldValue("ACT_PRICE");

      itemTotalPrice = itemLdbRows[i].getFieldValue("ITEM_TOTAL");
      lineQty = itemLdbRows[i].getIntegerFieldValue("ITEM_QTY");
      itemNo = itemLdbRows[i].getFieldValue("ITEM_NO");

      // Make sure 2 decimal places for each of the prices.
      //rrGstPrice = ((double)Math.round(rrGstPrice * 100)) / 100;
      //actGstPrice = ((double)Math.round(actGstPrice * 100)) / 100;
      //itemTotalPrice = ((double)Math.round(itemTotalPrice * 100)) / 100;

      stRrPrice = StringUtil.doubleToString(rrPrice, 2);
      stActPrice = StringUtil.doubleToString(actPrice, 2);
      //String stringitemTotalPrice = StringUtil.doubleToString(itemTotalPrice, 2);

      discount = (rrPrice - actPrice);
      //discount = ((double)Math.round(discount * 100)) / 100;
      stDiscount = StringUtil.doubleToString(discount, 2);

      if (discount > 0.01)
      {

        text.append(material + lineSeparator);
        text.append(matdesc + lineSeparator);
        text.append(spaces7 + lineQty + " @  $" + stRrPrice +
                    lineSeparator);
        text.append(spaces7 + "less $" + stDiscount +
                    lineSeparator);
        if (docType == RETURN)
        {
          text.append(spaces30 + "-$" + itemTotalPrice + "*" +
                      lineSeparator + lineSeparator);
        }
        else
        {
          text.append(spaces30 + "$" + itemTotalPrice + "*" +
                      lineSeparator + lineSeparator);
        }
      }
      else
      {
        text.append(material + lineSeparator);
        text.append(matdesc + lineSeparator);
        text.append(spaces7 + lineQty + " @  $" + stActPrice +
                    lineSeparator);
        if (docType == RETURN)
        {
          text.append(spaces30 + "-$" + itemTotalPrice + "*" +
                      lineSeparator + lineSeparator);
        }
        else
        {
          text.append(spaces30 + "$" + itemTotalPrice + "*" +
                      lineSeparator + lineSeparator);
        }
      }
    }

    payAmount = ( (double) Math.round(payAmount * 100)) / 100;
    totalChange = ( (double) Math.round(totalChange * 100)) / 100;

    orderGst = docHeaderLdbRows[0].getFieldValue("ORDER_GST");

    String sttotalChange = StringUtil.doubleToString(totalChange, 2);
    String sttotalOwing = StringUtil.doubleToString(totalOwing, 2);

    text.append(spaces + lineSeparator);

    if (docType == RETURN)
    {
      text.append("TOTAL (Inc GST)" + spaces15 + "-$" + receiptTotal + lineSeparator);
    }
    else
    {
      text.append("TOTAL (Inc GST)" + spaces15 + "$" + receiptTotal + lineSeparator);
    }

    text.append(spaces + lineSeparator);
    text.append("No. Items: " + itemLdbRows.length + lineSeparator);
    text.append(spaces + lineSeparator);
    text.append(spaces + lineSeparator);

    if (docType == RETURN)
    {
      String returnAmount = StringUtil.doubleToString( (payAmount * -1), 2);
      text.append(paymentType + spaces15 + "-$" + returnAmount + lineSeparator);
    }
    else
    {
      text.append(paymentType + spaces15 + "$" + stPayAmount + lineSeparator);
      text.append("Amount Owe" + spaces20 + "$" + sttotalOwing + lineSeparator);
      text.append("Change Due" + spaces20 + "$" + sttotalChange + lineSeparator);
    }

    text.append(spaces + lineSeparator);

    text.append("GST Amount" + spaces20 + "$" + orderGst + lineSeparator);
    text.append(spaces + lineSeparator);

    text.append("* Signifies item(s) with GST" + lineSeparator);
    text.append(spaces + lineSeparator);

    endText.append(spaces + lineSeparator);
    endText.append(spaces + lineSeparator);
    endText.append(spaces + lineSeparator);
    endText.append(spaces + lineSeparator);
    endText.append(spaces + lineSeparator);
    endText.append(spaces + lineSeparator);
    endText.append(spaces + lineSeparator);
    endText.append(spaces + lineSeparator);

    String printtext = text.toString();
    String endingText = endText.toString();

    //Build mapping to print variable
    VtiExitKeyValuePair[] keyValuePairs =
        {
        new VtiExitKeyValuePair("&ITEM&", printtext),
        new VtiExitKeyValuePair("&VTI_REF&", vtiRefNo),
        new VtiExitKeyValuePair("&NAME&", name),
        new VtiExitKeyValuePair("&DATE&", currDateP),
        new VtiExitKeyValuePair("&TIME&", currTimeP),
        new VtiExitKeyValuePair("&TEXT&", docText),
        new VtiExitKeyValuePair("&TEXT2&", docText2),
        new VtiExitKeyValuePair("&ENDDOC&", endingText),
    };

    try
    {
      if (docType == RETURN)
      {
        switch (deviceId)
        {
          case 1:
          {
            vtiUsrExt.invokePrintTemplate("RETURN1", keyValuePairs);
          }
          break;
          case 2:
          {
            vtiUsrExt.invokePrintTemplate("RETURN2", keyValuePairs);
          }
          break;
          case 3:
          {
            vtiUsrExt.invokePrintTemplate("RETURN3", keyValuePairs);
          }
          break;
          //case other:
          //return new VtiUserExitResult(999, "Invalid POS lane Configured");
        }
      }
      else
      {
        switch (deviceId)
        {
          case 1:
          {
            vtiUsrExt.invokePrintTemplate("RECEIPT1", keyValuePairs);
          }
          break;
          case 2:
          {
            vtiUsrExt.invokePrintTemplate("RECEIPT2", keyValuePairs);
          }
          break;
          case 3:
          {
            vtiUsrExt.invokePrintTemplate("RECEIPT3", keyValuePairs);
          }
          break;
          //return new VtiUserExitResult(999, "Invalid POS lane Configured");
        }
      }
    }
    catch (VtiExitException ee)
    {
      throw new VtiExitException("Print Error");
    }
  }
}
