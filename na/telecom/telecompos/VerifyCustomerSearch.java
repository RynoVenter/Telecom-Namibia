
package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class VerifyCustomerSearch extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
    // Data Declarations.     
    VtiUserExitScreenField ordTypField = getScreenField("ORDTYP");	  
    VtiUserExitScreenField kunnrField = getScreenField("KUNNR");	  	
    VtiUserExitScreenField ordValueField = getScreenField("ORDER_VALUE");	  	
	 
    if (ordTypField == null)
      return new VtiUserExitResult(999, "Screen Field ORDTYP does not exist");

    if (kunnrField == null)
      return new VtiUserExitResult(999, "Screen Field KUNNR does not exist");
	
    String orderTyp = ordTypField.getFieldValue();
	String kunnr = kunnrField.getFieldValue();
	String ordValue = ordValueField.getFieldValue();
    
    if (orderTyp.equals("CASH SALE"))
	{ 
		return new VtiUserExitResult(999, "Order type CASH SALE does not require a customer");
	}

	    if (orderTyp.equals("TAX EXEMPT"))
	{ 
		return new VtiUserExitResult(999, "Order type TAX EXEMPT does not require a customer");
	}

   if (orderTyp.equals("STAFF SALE"))
	{ 
		return new VtiUserExitResult(999, "Customer selection not required for STAFF SALE");
	}

    if (orderTyp.equals("TELLY POINT"))
	{ 
	  if (kunnr.length() > 0 || ordValue.length() > 0)
	  {	  
		return new VtiUserExitResult(999, "Order in process for customer!. Clear order for new selection");		  
	  }	
	}
	
    if (orderTyp.equals("TELLY WALKER"))
	{ 
	  if (kunnr.length() > 0 || ordValue.length() > 0)
	  {	  
		return new VtiUserExitResult(999, "Order in process for customer!. Clear order for new selection");		  
	  }	
	}	

    VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
	sessionHeader.setNextFunctionId("YSPS_TY_CUS_SRC");			

    return new VtiUserExitResult();
    
  }

}

       
