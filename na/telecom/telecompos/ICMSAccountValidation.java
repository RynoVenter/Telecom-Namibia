package na.telecom.telecompos;

import au.com.skytechnologies.vti.VtiExitException;

/*
•	ICMS Account Validation
For payments of which the account information is not transferred onto the POS system, 
the following account validation method/algorithm must be applied.

The last (rightmost) digit of the ICMS Account Number is a Modulus 11 self-check digit. 
If no positive account match in the POS system is found, 
the account number should be validated using this check digit.

When the account number is entered, the check digit should be stripped off and recalculated. 
If the entered value does not match the recalculated value, the account number should be rejected.

The Modulus 11 self-check digit is calculated as follows: 
•         Assign a weight factor to each digit position of the base number. 
These factors are: 2, 3, 4, 5, 6, 7, 2, 3, 4, 5, 6, 7, 2, 3,..... 
starting with the units position of the number and progressing toward the high-order digit. 

For example, the base number 10367585 would be assigned the weight factors as follows:
 
Base number       1 0 3 6 7 5 8 5
Weight factors    3 2 7 6 5 4 3 2
•         Multiply each digit by its weight factor.
•         Add the products. 
•         Divide this sum by 11. 
•         Subtract the remainder from 11. 
•         The difference is the self-check digit. 

Example: 

Base number:                               1     0     3     6     7     5     8     5
Weight factors:                            3     2     7     6     5     4     3     2
Multiply each digit by its weight factor:  3     0    21    36    35    20    24    10

Add the products: 3 + 0 + 21 + 36 + 35 + 20 + 24 + 10 = 149 

Divide the sum by 11: 149/11 = 13 plus a remainder of 6. 

Subtract the remainder from 11: 11 - 6 = 5 

Self-check digit is 5. The self checking account number is 103675855.
 
Note: If the remainder in the above step is 0, the self-check digit is 0. If the remainder is 1, the base number has no self-check digit. That sort of base numbers is not used for account numbers. 
*/


public class ICMSAccountValidation {
	
	private final static int MODULUS_11 = 11;
	
	public static boolean CheckAlgorithm (String strAccountNo) throws VtiExitException
	{   
		//Get the base account No
		String strBaseAccountNo = strAccountNo.substring(0,strAccountNo.length()-1);
		
		//Get the check digit
		int intCheckDigit = Integer.parseInt(strAccountNo.substring(strAccountNo.length()-1));
		
		//Calculated check digit declaration
		int intCalculatedCheckDigit = 0;
		
		int intWeightFactor = 2;
		int intBaseAccountPosition = 0;
		int intTotalProducts = 0;
		int intMODValue = 0;
		int intProductWeight = 0;
		
		
		//working backwards
		for(int i=strBaseAccountNo.length(); i>0; i--)
		{
			//Base value digit
			intBaseAccountPosition = Integer.parseInt(strBaseAccountNo.substring(i-1, i));
			
			//Calculate the weight
			intProductWeight = intBaseAccountPosition * intWeightFactor;
			
			//Add the products
			intTotalProducts += intProductWeight;
			
			intWeightFactor+=1;
			
			//Weight factor should start from 2,3,4,5,6,7..back to 2
			if(intWeightFactor>7)
			{
				intWeightFactor = 2;
			}
		}
		
		//get the mod value
		intMODValue = intTotalProducts % MODULUS_11;
		
		if(intMODValue == 1)
			return false;
		
		if(intMODValue != 0)
			intCalculatedCheckDigit = MODULUS_11 - intMODValue;
		else
			intCalculatedCheckDigit = 0;
									 
		if(intCheckDigit != intCalculatedCheckDigit)
		{
			return false;
		}
		
		return true;
	}

}
