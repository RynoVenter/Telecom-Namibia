package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CheckRecQty extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
    // Data Declarations.     
    VtiUserExitScreenField TRField = getScreenField("REC_TR");	  
    VtiUserExitScreenField IssueField = getScreenField("REC_TRQTY");
    VtiUserExitScreenField CIssueField = getScreenField("C_REC_QTY");
	 
    if (TRField == null)
      return new VtiUserExitResult(999, "Screen Field REC_TR does not exist");

    String TrNo = TRField.getFieldValue();
    
    if (TrNo.length() == 0)
        return new VtiUserExitResult(999, "No Transfer Order has been selected");    
	
    if (IssueField == null)
      return new VtiUserExitResult(999, "Screen Field =REC_TRQTY does not exist");
    
    int IssQty = IssueField.getIntegerFieldValue();
    
    if (CIssueField == null)
      return new VtiUserExitResult(999, "Screen Field C_REC_QTY does not exist");
    
    int CIssQty = CIssueField.getIntegerFieldValue();	
	
    if ( IssQty != CIssQty)
        return new VtiUserExitResult(999, "Receipt Qty is Different to Issued Qty");    
    
    if ( IssQty == 0 )
        return new VtiUserExitResult(999, "Receipt Qty must be greater than zero");    
	
    return new VtiUserExitResult();
    
  }
}