package na.telecom.telecompos;
							 
import java.text.*;
import java.util.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;

public class TillReport
    extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
    VtiUserExitScreenField userIdField = getScreenField("REP_ID");
    VtiUserExitScreenField firstNameField = getScreenField("FIRST_NAME");
    VtiUserExitScreenField lastNameField = getScreenField("LAST_NAME");
    VtiUserExitScreenField frDateField = getScreenField("FR_DATE");
    VtiUserExitScreenField toDateField = getScreenField("TO_DATE");
    VtiUserExitScreenField frTimeField = getScreenField("FR_TIME");
    VtiUserExitScreenField toTimeField = getScreenField("TO_TIME");
    VtiUserExitScreenField payTypeField = getScreenField("PAYMENT_TYPE");
    VtiUserExitScreenField posLaneField = getScreenField("POS_LANE");
    VtiUserExitScreenField posDescField = getScreenField("DESCRIPTION");

    if (userIdField == null)
    {
      return new VtiUserExitResult(999, "Field REP_ID does not exist");
    }
    if (firstNameField == null)
    {
      return new VtiUserExitResult(999, "Field FIRST_NAME does not exist");
    }
    if (lastNameField == null)
    {
      return new VtiUserExitResult(999, "Field LAST_NAME does not exist");
    }
    if (frDateField == null)
    {
      return new VtiUserExitResult(999, "Field FR_DATE does not exist");
    }
    if (toDateField == null)
    {
      return new VtiUserExitResult(999, "Field TO_DATE does not exist");
    }
    if (frTimeField == null)
    {
      return new VtiUserExitResult(999, "Field FR_TIME does not exist");
    }
    if (toTimeField == null)
    {
      return new VtiUserExitResult(999, "Field TO_TIME does not exist");
    }
    if (payTypeField == null)
    {
      return new VtiUserExitResult(999, "Field PAYMENT_TYPE does not exist");
    }
    if (posLaneField == null)
    {
      return new VtiUserExitResult(999, "Field POS_LANE does not exist");
    }
    if (posDescField == null)
    {
      return new VtiUserExitResult(999, "Field DESCRIPTION does not exist");
    }

    VtiExitLdbTable paymentLdbTable =
        getLocalDatabaseTable("YSPS_PAYMENT_TRANSACTION");

    if (paymentLdbTable == null)
    {
      return new VtiUserExitResult
          (999, "LDB table YSPS_PAYMENT_TRANSACTION not found");
    }

    // Get the session header information.
    VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();

    if (sessionHeader == null)
    {
      return new VtiUserExitResult(999, "Error Retrieving Session Header Info");
    }

    int deviceId = sessionHeader.getDeviceNumber();
    String stringDeviceId = Integer.toString(deviceId);

    String vtiServerId = getVtiServerId();
    String userId = userIdField.getFieldValue();
    String firstName = firstNameField.getFieldValue();
    String lastName = lastNameField.getFieldValue();
    String frDate = frDateField.getFieldValue();
    String toDate = toDateField.getFieldValue();
    String frTime = frTimeField.getFieldValue();
    int intFrTime = frTimeField.getIntegerFieldValue();
    String toTime = toTimeField.getFieldValue();
    int intToTime = toTimeField.getIntegerFieldValue();
    String payType = payTypeField.getFieldValue();
    int posLane = posLaneField.getIntegerFieldValue();
    String posDesc = posDescField.getFieldValue();
    String preSale = " ";

    // Get constants used for generating the file
    String lineSeparator = System.getProperty("line.separator");
    String spaces = " ";
    StringBuffer detText = new StringBuffer();
    StringBuffer sumText = new StringBuffer();

    // Build the base selection criteria
    VtiExitLdbSelectCriterion[] gr1DateSelConds =
        {
        new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId),
        new VtiExitLdbSelectCondition("PAY_DATE", VtiExitLdbSelectCondition.GT_OPERATOR, frDate)
    };

    VtiExitLdbSelectConditionGroup gr1DateSelCondGrp = new VtiExitLdbSelectConditionGroup(gr1DateSelConds, true);

    VtiExitLdbSelectCriterion[] gr2DateSelConds =
        {
        new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId),
        new VtiExitLdbSelectCondition("PAY_DATE", VtiExitLdbSelectCondition.EQ_OPERATOR, frDate),
        new VtiExitLdbSelectCondition("PAY_TIME", VtiExitLdbSelectCondition.GE_OPERATOR, frTime)
    };

    VtiExitLdbSelectConditionGroup gr2DateSelCondGrp = new VtiExitLdbSelectConditionGroup(gr2DateSelConds, true);

    VtiExitLdbSelectCriterion[] gr3DateSelConds =
        {
        gr1DateSelCondGrp,
        gr2DateSelCondGrp
    };

    VtiExitLdbSelectConditionGroup gr3DateSelCondGrp = new VtiExitLdbSelectConditionGroup(gr3DateSelConds, false);

    // below are the "to date" selection groups.
    VtiExitLdbSelectCriterion[] gr4DateSelConds =
        {
        new VtiExitLdbSelectCondition("SERVERID",  VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId),
        new VtiExitLdbSelectCondition("PAY_DATE", VtiExitLdbSelectCondition.LE_OPERATOR, toDate)
    };

    VtiExitLdbSelectConditionGroup gr4DateSelCondGrp = new VtiExitLdbSelectConditionGroup(gr4DateSelConds, true);

    VtiExitLdbSelectCriterion[] gr5DateSelConds =
        {
        new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId),
        new VtiExitLdbSelectCondition("PAY_DATE", VtiExitLdbSelectCondition.EQ_OPERATOR, toDate),
        new VtiExitLdbSelectCondition("PAY_TIME", VtiExitLdbSelectCondition.LT_OPERATOR, toTime)
    };

    VtiExitLdbSelectConditionGroup gr5DateSelCondGrp = new VtiExitLdbSelectConditionGroup(gr5DateSelConds, true);

    VtiExitLdbSelectCriterion[] gr6DateSelConds =
        {
        gr4DateSelCondGrp,
        gr5DateSelCondGrp
    };

    VtiExitLdbSelectConditionGroup gr6DateSelCondGrp = new VtiExitLdbSelectConditionGroup(gr6DateSelConds, false);

    VtiExitLdbSelectCriterion[] totalDateSelConds =
        {
        gr3DateSelCondGrp,
        gr6DateSelCondGrp
    };

    VtiExitLdbSelectConditionGroup totalDateSelCondGrp = new VtiExitLdbSelectConditionGroup(totalDateSelConds, true);

    // If only a particular payment type is required.
    if (!(payType.equals("ALL")))
    {
      totalDateSelCondGrp.addCriterion(new VtiExitLdbSelectCondition("PAYMENT_TYPE", VtiExitLdbSelectCondition.EQ_OPERATOR, payType));
    }

    // If a particular POS lane was selected.
    String stPosLane = Integer.toString(posLane);

    if (posLane > 0)
    {
      totalDateSelCondGrp.addCriterion(new VtiExitLdbSelectCondition("POS_LANE", VtiExitLdbSelectCondition.EQ_OPERATOR, stPosLane));
    }

    VtiExitLdbOrderSpecification payTypeOrder[] =
        {
        new VtiExitLdbOrderSpecification("PAYMENT_TYPE"),
        new VtiExitLdbOrderSpecification("PAY_DATE"),
        new VtiExitLdbOrderSpecification("PAY_TIME"),
    };

    // Fetch the corresponding items from the PAYMENT_TRANSACTION
    // LDB table.
    VtiExitLdbTableRow[] paymentLdbRows =
        paymentLdbTable.getMatchingRows(totalDateSelCondGrp, payTypeOrder);

    if (paymentLdbRows.length == 0)
    {
      return new VtiUserExitResult(999, "No payments were found for the selected options");
    }

    // Loop through all of the payments and total up the value for each payment type
    String paymentType = "";
    String payDesc = "";
    String payDate = "";
    String payTime = "";
    String varSpaces = "";
    int vtiRef = 0;
    int posId = 0;

    double cashTotal = 0.00;
    double eftDdTotal = 0.00;
    double eftCcTotal = 0.00;
    double persChqTotal = 0.00;
    double compChqTotal = 0.00;
    double bankChqTotal = 0.00;
    double directTotal = 0.00;
    double giftVouchTotal = 0.00;
    double flexirentTotal = 0.00;
    double letterCommitTotal = 0.00;
    double eftposTotal = 0.00;

    double payAmount = 0.00;

    // Format the date and time for each row.
    SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    sDateFormat.setTimeZone(Calendar.getInstance().getTimeZone());

    //String dateTime = "";
    Date formatDate = new Date();

    /*
             for (int i = 0; i < paymentLdbRows.length; ++i)
       {
        String voucherNo = "";
        preSale = paymentLdbRows[i].getFieldValue("PRESALE");
        paymentType = paymentLdbRows[i].getFieldValue("PAYMENT_TYPE");
        payDate = paymentLdbRows[i].getStringFieldValue("PAY_DATE");
        int intPayTime = paymentLdbRows[i].getIntegerFieldValue("PAY_TIME");
        payAmount = paymentLdbRows[i].getDoubleFieldValue("AMOUNT");
        vtiRef = paymentLdbRows[i].getIntegerFieldValue("VTI_REF");
        posId = paymentLdbRows[i].getIntegerFieldValue("POS_LANE");

        if (paymentType.equals("CASH"))
        {
            cashTotal += payAmount;
            varSpaces = "           ";  // 12 spaces for alignment
        }
        else if (paymentType.equals("EFT_DD"))
        {
            eftDdTotal += payAmount;
            varSpaces = "         ";  // 10 spaces for alignment
        }
        else if (paymentType.equals("EFT_CC"))
        {
            eftCcTotal += payAmount;
            varSpaces = "         ";  // 10 spaces for alignment
        }
        else if (paymentType.equals("PERSONAL_CHQ"))
        {
            persChqTotal += payAmount;
            varSpaces = "   ";  // 4 spaces for alignment
        }
        else if (paymentType.equals("COMPANY_CHQ"))
        {
            compChqTotal += payAmount;
            varSpaces = "    ";  // 5 spaces for alignment
        }
        else if (paymentType.equals("BANK_CHQ"))
        {
            bankChqTotal += payAmount;
            varSpaces = "       ";  // 8 spaces for alignment
        }
        else if (paymentType.equals("DIRECT_DEBIT"))
        {
            directTotal += payAmount;
              varSpaces = "   ";  // 4 spaces for alignment
        }
        else if (paymentType.equals("GIFT_VOUCHER"))
        {
            giftVouchTotal += payAmount;
            varSpaces = "   ";  // 4 spaces for alignment
            voucherNo = paymentLdbRows[i].getFieldValue("VOUCHER_NO");
            if(voucherNo.length() > 14)
                voucherNo = StringUtil.stringExactLength(voucherNo, 14);
        }
        else
            return new VtiUserExitResult(999, "Unknown Payment Type Encountered");

        // Set a space variable, depending on the length of the variable before
        String stPayAmount = StringUtil.doubleToString(payAmount, 2);
        String stVtiRef = Integer.toString(vtiRef);
        String stPosId = Integer.toString(posId);

        // total length 12 for amount
        stPayAmount = StringUtil.stringPad(stPayAmount, 9);

        // VTI reference field length 10
        stVtiRef = StringUtil.stringPad(stVtiRef, 7);

        // POS Lane length 10
        stPosId = StringUtil.stringPad(stPosId, 5);

        // Presale length 7
        preSale = StringUtil.stringPad(preSale, 7);

        //DateFormatter.
        if (intPayTime < 100000)
        {
            payTime = Integer.toString(intPayTime);
            payTime = StringUtil.stringPad(payTime, 6, '0');
        }
        else
            payTime = Integer.toString(intPayTime);

        dateTime = payDate + payTime;

        try
           {
      formatDate = sDateFormat.parse(dateTime);
           }
           catch (ParseException ee)
           {
     return new VtiUserExitResult(999, "Error Formatting the date/Time");
           }

        payDate = DateFormatter.format("dd/MM/yyyy", formatDate);
        payTime = DateFormatter.format("HH:mm:ss", formatDate);

        detText.append(payDate + spaces +
           payTime + spaces +
           paymentType + varSpaces +
                    stPayAmount + spaces +
                    stVtiRef + spaces +
                    stPosId + spaces +
                    preSale + voucherNo + lineSeparator);

             }

             double reportTotal = bankChqTotal + cashTotal + directTotal
        + eftCcTotal + eftDdTotal + giftVouchTotal + persChqTotal;

             // Round the total to get rid of any "funny" Java rounding.
             String streportTotal = StringUtil.doubleToString(reportTotal, 2);
             String stbankChqTotal = StringUtil.doubleToString(bankChqTotal, 2);
             String stcashTotal = StringUtil.doubleToString(cashTotal, 2);
             String stdirectTotal = StringUtil.doubleToString(directTotal, 2);
             String steftCcTotal = StringUtil.doubleToString(eftCcTotal, 2);
             String steftDdTotal = StringUtil.doubleToString(eftDdTotal, 2);
             String stgiftVouchTotal = StringUtil.doubleToString(giftVouchTotal, 2);
             String stpersChqTotal = StringUtil.doubleToString(persChqTotal, 2);
             String stcompChqTotal = StringUtil.doubleToString(compChqTotal, 2);
             String stReportTotal = StringUtil.doubleToString(reportTotal, 2);

             // Write the values out to a file.
             String detailText = detText.toString();

             // Populate the Summary
             sumText.append("Bank Cheque Total" + "        " + "$ " +
                   stbankChqTotal + lineSeparator);
             sumText.append("Cash Total" + "               " + "$ " +
                   stcashTotal + lineSeparator);
             sumText.append("Direct Debit Total" + "       " + "$ " +
                   stdirectTotal + lineSeparator);
             sumText.append("EFT Credit Card Total" + "    "  + "$ " +
                   steftCcTotal + lineSeparator);
             sumText.append("EFT Debit Card Total" + "     " + "$ " +
                   steftDdTotal + lineSeparator);
             sumText.append("Gift Voucher Total" + "       "  + "$ " +
                   stgiftVouchTotal + lineSeparator);
             sumText.append("Personal Cheque Total" + "    "  + "$ " +
                   stpersChqTotal + lineSeparator);
             sumText.append("Company Cheque Total" + "     "  + "$ " +
                   stcompChqTotal + lineSeparator);

             String summaryText = sumText.toString();


     */
    //***********************************************************

    for (int i = 0; i < paymentLdbRows.length; ++i)
    {
      String voucherNo = "";
      preSale = paymentLdbRows[i].getFieldValue("PRESALE");
      paymentType = paymentLdbRows[i].getFieldValue("PAYMENT_TYPE");
      payDate = paymentLdbRows[i].getFieldValue("PAY_DATE");
      int intPayTime = paymentLdbRows[i].getIntegerFieldValue("PAY_TIME");
      payAmount = paymentLdbRows[i].getDoubleFieldValue("AMOUNT");
      vtiRef = paymentLdbRows[i].getIntegerFieldValue("VTI_REF");
      posId = paymentLdbRows[i].getIntegerFieldValue("POS_LANE");

      if (paymentType.equals("CASH"))
      {
        cashTotal += payAmount;
        payDesc = "Cash";
      }

      else if (paymentType.equals("GIFT_VOUCHER"))
      {
        giftVouchTotal += payAmount;
        payDesc = "Gift Voucher";
        voucherNo = paymentLdbRows[i].getFieldValue("VOUCHER_NO");
      }

      else if (paymentType.equals("DEBIT_CARD"))
      {
        eftDdTotal += payAmount;
        payDesc = "Debit Card";
      }

      else if (paymentType.equals("CREDIT_CARD"))
      {
        eftCcTotal += payAmount;
        payDesc = "Credit Card";
      }

      else if (paymentType.equals("EFTPOS"))
      {
        eftposTotal += payAmount;
        payDesc = "Eftpos";
      }

      else if (paymentType.equals("PERSONAL_CHQ"))
      {
        persChqTotal += payAmount;
        payDesc = "Pers Cheque";
      }

      else if (paymentType.equals("COMPANY_CHQ"))
      {
        compChqTotal += payAmount;
        payDesc = "Company Cheque";
      }

      else if (paymentType.equals("BANK_CHQ"))
      {
        bankChqTotal += payAmount;
        payDesc = "Bank Cheque";
      }

      else if (paymentType.equals("DIRECT_DEBIT"))
      {
        directTotal += payAmount;
        payDesc = "Direct Debit";
      }
      else if (paymentType.equals("COMMIT_LETTER"))
      {
        letterCommitTotal += payAmount;
        payDesc = "Commit Letter";
      }
      else if (paymentType.equals("FLEXIRENT"))
      {
        flexirentTotal += payAmount;
        payDesc = "Flexirent";
      }

      //else
      //{
      //    Log.error(paymentLdbRows[i].getFieldValue("VTI_REF"));
      //    Log.error(paymentLdbRows[i].getFieldValue("PAYMENT_SEQ"));
      //    Log.error(paymentType);
      //    return new VtiUserExitResult(999, "Unknown Payment Type Encountered");
      //}

      // Set a space variable, depending on the length of the variable before
      String stPayAmount = StringUtil.doubleToString(payAmount, 2);
      String stVtiRef = Integer.toString(vtiRef);
      String stPosId = Integer.toString(posId);

      // Pad out each of the String Fields.
      payDesc = StringUtil.stringPad(payDesc, 15);
      stPayAmount = StringUtil.stringPad(stPayAmount, 10);
      stVtiRef = StringUtil.stringPad(stVtiRef, 8);
      stPosId = StringUtil.stringPad(stPosId, 6);
      preSale = StringUtil.stringPad(preSale, 7);

      if (voucherNo.length() > 14)
      {
        voucherNo = StringUtil.stringExactLength(voucherNo, 14);
      }
      else
      {
        voucherNo = StringUtil.stringPad(voucherNo, 14);

        // Format the date and time
        //SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        //sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
      }
      if (intPayTime < 100000)
      {
        payTime = Integer.toString(intPayTime);
        payTime = StringUtil.stringPad(payTime, 6, '0');
      }
      else
      {
        payTime = Integer.toString(intPayTime);

      }
      String dateTime = payDate + payTime;
      //formatDate = new Date();

      sDateFormat.setTimeZone(Calendar.getInstance().getTimeZone());

      try
      {
        formatDate = sDateFormat.parse(dateTime);
      }
      catch (ParseException gg)
      {
        return new VtiUserExitResult(999, "Error Converting String to Date");
      }

      payDate = DateFormatter.format("dd/MM/yyyy", formatDate);
      payTime = DateFormatter.format("HH:mm:ss", formatDate);
      payDate = StringUtil.stringPad(payDate, 11);
      payTime = StringUtil.stringPad(payTime, 9);

      detText.append(payDate);
      detText.append(payTime);
      detText.append(payDesc);
      detText.append(stPayAmount);
      detText.append(stVtiRef);
      detText.append(stPosId);
      detText.append(preSale);
      detText.append(voucherNo);
      detText.append(lineSeparator);

      paymentLdbRows[i].setFieldValue("TILL_REC_FLAG", "X");
      paymentLdbRows[i].setFieldValue("TIMESTAMP", "");

      // Save the Updated Payment.
      try
      {
        paymentLdbTable.saveRow(paymentLdbRows[i]);
      }
      catch (VtiExitException kk)
      {
        return new VtiUserExitResult(999, "Error Updating the Reconcilled Payment");
      }
    }

    double reportTotal = bankChqTotal + cashTotal + directTotal
        + eftCcTotal + eftDdTotal + giftVouchTotal
        + persChqTotal + bankChqTotal + compChqTotal
        + flexirentTotal + letterCommitTotal;

    // Round the total to get rid of any "funny" Java rounding.

    String stbankChqTotal = StringUtil.doubleToString(bankChqTotal, 2);
    String stcashTotal = StringUtil.doubleToString(cashTotal, 2);
    String stcompChqTotal = StringUtil.doubleToString(compChqTotal, 2);
    String steftCcTotal = StringUtil.doubleToString(eftCcTotal, 2);
    String steftDdTotal = StringUtil.doubleToString(eftDdTotal, 2);
    String stdirectTotal = StringUtil.doubleToString(directTotal, 2);
    String steftposTotal = StringUtil.doubleToString(eftposTotal, 2);
    String stflexirentTotal = StringUtil.doubleToString(flexirentTotal, 2);
    String stgiftVouchTotal = StringUtil.doubleToString(giftVouchTotal, 2);
    String stLetterTotal = StringUtil.doubleToString(letterCommitTotal, 2);
    String stpersChqTotal = StringUtil.doubleToString(persChqTotal, 2);

    String stReportTotal = StringUtil.doubleToString(reportTotal, 2);

    // Write the values out to a file.
    String detailText = detText.toString();

    // Populate the Summary
    if (bankChqTotal != 0)
    {
      sumText.append("Bank Cheque Total        " + "$ " +
                     stbankChqTotal + lineSeparator);
    }
    if (cashTotal != 0)
    {
      sumText.append("Cash Total               " + "$ " +
                     stcashTotal + lineSeparator);
    }
    if (compChqTotal != 0)
    {
      sumText.append("Company Cheque Total     " + "$ " +
                     stcompChqTotal + lineSeparator);
    }
    if (eftCcTotal != 0)
    {
      sumText.append("Credit Card Total        " + "$ " +
                     steftCcTotal + lineSeparator);
    }
    if (eftDdTotal != 0)
    {
      sumText.append("Debit Card Total         " + "$ " +
                     steftDdTotal + lineSeparator);
    }
    if (directTotal != 0)
    {
      sumText.append("Direct Debit Total       " + "$ " +
                     stdirectTotal + lineSeparator);
    }
    if (eftposTotal != 0)
    {
      sumText.append("Eftpos Total             " + "$ " +
                     steftposTotal + lineSeparator);
    }
    if (flexirentTotal != 0)
    {
      sumText.append("Flexirent Total          " + "$ " +
                     stflexirentTotal + lineSeparator);
    }
    if (giftVouchTotal != 0)
    {
      sumText.append("Gift Voucher Total       " + "$ " +
                     stgiftVouchTotal + lineSeparator);
    }
    if (letterCommitTotal != 0)
    {
      sumText.append("Letter of Commit Total   " + "$ " +
                     stLetterTotal + lineSeparator);
    }
    if (persChqTotal != 0)
    {
      sumText.append("Personal Cheque Total    " + "$ " +
                     stpersChqTotal + lineSeparator);

    }
    String summaryText = sumText.toString();

    //****************************************************
     // Setup current date
    String currDate = DateFormatter.format("dd/MM/yyyy");
    String currTime = DateFormatter.format("HH:mm:ss");

    // Format the From selection date and times.
    if (intFrTime < 100000)
    {
      frTime = Integer.toString(intFrTime);
      frTime = StringUtil.stringPad(frTime, 6, '0');
    }
    else
    {
      payTime = Integer.toString(intFrTime);

    }
    String fromDateTime = frDate + frTime;
    try
    {
      formatDate = sDateFormat.parse(fromDateTime);
    }
    catch (ParseException ee)
    {
      return new VtiUserExitResult(999, "Error Formatting the From date/time");
    }

    frDate = DateFormatter.format("dd/MM/yyyy", formatDate);
    frTime = DateFormatter.format("HH:mm:ss", formatDate);

    // Format the To Selection Date and Time
    if (intToTime < 100000)
    {
      toTime = Integer.toString(intToTime);
      toTime = StringUtil.stringPad(toTime, 6, '0');
    }
    else
    {
      toTime = Integer.toString(intToTime);

    }
    String toDateTime = toDate + toTime;

    try
    {
      formatDate = sDateFormat.parse(toDateTime);
    }
    catch (ParseException ee)
    {
      return new VtiUserExitResult(999, "Error Formatting the TO date/time");
    }

    toDate = DateFormatter.format("dd/MM/yyyy", formatDate);
    toTime = DateFormatter.format("HH:mm:ss", formatDate);

    // Build mapping to print variable
    VtiExitKeyValuePair[] keyValuePairs =
        {
        new VtiExitKeyValuePair("&USERID&", userId),
        new VtiExitKeyValuePair("&FIRSTNAME&", firstName),
        new VtiExitKeyValuePair("&LASTNAME&", lastName),
        new VtiExitKeyValuePair("&DATE&", currDate),
        new VtiExitKeyValuePair("&TIME&", currTime),
        new VtiExitKeyValuePair("&FRDATE&", frDate),
        new VtiExitKeyValuePair("&FRTIME&", frTime),
        new VtiExitKeyValuePair("&TODATE&", toDate),
        new VtiExitKeyValuePair("&TOTIME&", toTime),
        new VtiExitKeyValuePair("&PAYTYPE&", payType),
        new VtiExitKeyValuePair("&SUMMARY&", summaryText),
        new VtiExitKeyValuePair("&TOTAL&", stReportTotal),
        new VtiExitKeyValuePair("&DETAIL&", detailText),
        new VtiExitKeyValuePair("&POSDESC&", posDesc),
    };

    try
    {
      invokePrintTemplate("TillRepPrint", keyValuePairs);
    }
    catch (VtiExitException ee)
    {
    }

    return new VtiUserExitResult(000, "Till Reconciliation Report Generated");
  }
}
