package na.telecom.telecompos;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class GetLastOrder extends VtiUserExit 
{
	
		  public VtiUserExitResult execute() throws VtiExitException
		  {
			  
				VtiUserExitScreenField ordNo = getScreenField("VTI_REF");
				if(ordNo == null) new VtiUserExitResult(999,"The following screen field, VTI_REF, did not load.");
				
				VtiExitLdbTable docHeaderLdbTable = getLocalDatabaseTable("YSPS_DOC_HEADER");
				if(docHeaderLdbTable == null) new VtiUserExitResult(999,"The following ldb, YSPS_DOC_HEADER, did not load.");
		  
				
				VtiExitLdbSelectCriterion prnHeaderSelConds = new VtiExitLdbSelectCondition("VTI_REF",VtiExitLdbSelectCondition.GE_OPERATOR, "1");
        
				VtiExitLdbSelectConditionGroup prnHeaderSelCondGrp = new VtiExitLdbSelectConditionGroup(prnHeaderSelConds, true);
				VtiExitLdbTableRow[] prnHeaderLdbRows = docHeaderLdbTable.getMatchingRows(prnHeaderSelCondGrp);
				
				ordNo.setFieldValue(prnHeaderLdbRows [prnHeaderLdbRows.length - 1].getFieldValue("VTI_REF"));

				return new  VtiUserExitResult();
		  }
}
