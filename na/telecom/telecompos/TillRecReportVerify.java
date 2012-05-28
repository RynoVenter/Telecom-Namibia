package na.telecom.telecompos;							 

import au.com.skytechnologies.vti.*;

public class TillRecReportVerify
    extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
    VtiUserExitScreenField tillRecMsgField = getScreenField("TILL_REC_MSG");
    VtiUserExitScreenField frDateField = getScreenField("FR_DATE");
    VtiUserExitScreenField toDateField = getScreenField("TO_DATE");
    VtiUserExitScreenField frTimeField = getScreenField("FR_TIME");
    VtiUserExitScreenField toTimeField = getScreenField("TO_TIME");
    VtiUserExitScreenField payTypeField = getScreenField("PAYMENT_TYPE");
    VtiUserExitScreenField posDescriptionField = getScreenField("DESCRIPTION");

    if (tillRecMsgField == null)
    {
      return new VtiUserExitResult(999, "Field TILL_REC_MSG does not exist");
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
    if (posDescriptionField == null)
    {
      return new VtiUserExitResult(999, "Field DESCRIPTION does not exist");
    }

    // Check to see if all the required selection parameters have been entered.

    String frDate = frDateField.getFieldValue();
    String frTime = frTimeField.getFieldValue();
    String toDate = toDateField.getFieldValue();
    String toTime = toTimeField.getFieldValue();
    String payType = payTypeField.getFieldValue();

    if (frDate.equals(""))
    {
      setCursorPosition(frDateField);
      return new VtiUserExitResult(999, "Please enter a from date");
    }
    if (toDate.equals(""))
    {
      setCursorPosition(toDateField);
      return new VtiUserExitResult(999, "Please enter a to date");
    }
    if (frTime.equals(""))
    {
      setCursorPosition(frTimeField);
      return new VtiUserExitResult(999, "Please enter a from time");
    }
    if (toTime.equals(""))
    {
      setCursorPosition(toTimeField);
      return new VtiUserExitResult(999, "Please enter a to time");
    }
    if (payType.equals(""))
    {
      setCursorPosition(payTypeField);
      return new VtiUserExitResult(999, "Please select one of the options for payment type");
    }
    if (posDescriptionField.equals(""))
    {
      setCursorPosition(posDescriptionField);
      return new VtiUserExitResult(999, "Please select one of the options for POS Lane");
    }

    return new VtiUserExitResult();
  }
}
