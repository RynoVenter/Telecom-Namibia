package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class CheckIssQty extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
    // Data Declarations.     
    VtiUserExitScreenField TRField = getScreenField("ISS_TO");	  
    VtiUserExitScreenField IssueField = getScreenField("ISSUE_QTY");
    VtiUserExitScreenField CIssueField = getScreenField("C_ISSUE_QTY");
	 
    if (TRField == null)
      return new VtiUserExitResult(999, "Screen Field ISS_TO does not exist");

    String TrNo = TRField.getFieldValue();
    
    if (TrNo.length() == 0)
        return new VtiUserExitResult(999, "No Transfer Order has been selected");    
	
    if (IssueField == null)
      return new VtiUserExitResult(999, "Screen Field ISSUE_QTY does not exist");
    
    int IssQty = IssueField.getIntegerFieldValue();
    
    if (CIssueField == null)
      return new VtiUserExitResult(999, "Screen Field C_ISSUE_QTY does not exist");
    
    int CIssQty = CIssueField.getIntegerFieldValue();	
	
    if ( IssQty != CIssQty)
        return new VtiUserExitResult(999, "Issued Qty is Different to Reqested Qty");    
    
    if ( IssQty == 0 )
        return new VtiUserExitResult(999, "Issued Qty must be greater than zero");    
	
    return new VtiUserExitResult();
    
  }
}