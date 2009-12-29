package edu.stanford;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

/**
* junit4 tests for Stanford University 
*  lopping volume information from call numbers only in the context of multiple occurrences of items in the same
*    library - home location - callnum scheme  
*  combination.
* @author Naomi Dushay
*/
public class CallNumLibLocComboLopTests extends AbstractStanfordBlacklightTest {

	private String fldName = "shelfkey";
	private String fileName = "callNumLibLocVolLopTests.mrc";
    private String testFilePath = testDataParentPath + File.separator + fileName;
    
	private String lcCallnumVol1 = "E184.S75 R47 V.1";
	private String lcCallnumVol2 = "E184.S75 R47 V.2";
	private String lcCallnumVol3 = "E184.S75 R47 V.3";
	private String lcLoppedCallnum = "E184.S75 R47";
	private String lcVol1Shelfkey = CallNumUtils.getShelfKey(lcCallnumVol1, lcScheme, null).toLowerCase();
	private String lcVol2Shelfkey = CallNumUtils.getShelfKey(lcCallnumVol2, lcScheme, null).toLowerCase();
	private String lcVol3Shelfkey = CallNumUtils.getShelfKey(lcCallnumVol3, lcScheme, null).toLowerCase();
	private String lcLoppedShelfkey = CallNumUtils.getShelfKey(lcLoppedCallnum, lcScheme, null).toLowerCase();

	private String deweyCallnumVol1 = "352.042 .C594 ED.1";
	private String deweyCallnumVol2 = "352.042 .C594 ED.2";
	private String deweyLoppedCallnum = "352.042 .C594";
	private String deweyVol1Shelfkey = CallNumUtils.getShelfKey(deweyCallnumVol1, deweyScheme, null).toLowerCase();
	private String deweyVol2Shelfkey = CallNumUtils.getShelfKey(deweyCallnumVol2, deweyScheme, null).toLowerCase();
	private String deweyLoppedShelfkey = CallNumUtils.getShelfKey(deweyLoppedCallnum, deweyScheme, null).toLowerCase();


@Before
	public final void setup() 
	{
		mappingTestInit();
	}	

	/**
	 * If there is only one non-skipped 999, then don't lop call number
	 */
@Test
	public void testSingleItemRecordsNotLopped() 
	{
		String id = "oneLC999";
		assertLCVol1NotLopped(id);
		assertNoLoppedLC(id);
	    
	    id = "oneDewey999";
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, deweyVol1Shelfkey);

	    id = "oneAlphanumBox";
	    String callnum = "M1522 BOX 1";
	    String shelfkey = CallNumUtils.getShelfKey(callnum, otherScheme, id).toLowerCase();
		solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, shelfkey);
	}

	/**
	 * If there is only one item per building, don't lop
	 */
@Test
	public void testDiffLibs() 
	{
		String id = "diffLib";
		assertLCVol1NotLopped(id);
		assertLCVol2NotLopped(id);
		assertNoLoppedLC(id);
	}

	/**
	 * If there is only one item per location in same building, don't lop
	 */
@Test
	public void testSameLibDiffLocs() 
	{
		String id = "diffHomeLoc";
		assertLCVol1NotLopped(id);
		assertLCVol2NotLopped(id);
		assertNoLoppedLC(id);
	}    

	/**
	 * If there is only one item per scheme in same lib/location, don't lop
	 */
@Test
	public void testSameLibSameLocDiffScheme() 
	{
		String id = "diffScheme";
		assertLCVol1NotLopped(id);
		assertNoLoppedLC(id);
		assertDeweyVol1NotLopped(id);
		assertNoLoppedDewey(id);
	}    

	/**
	 * lop when only diff is scheme LC and LCPER
	 */
@Test
	public void testLopLCSchemeFlavors() 
	{
		String id = "lcFlavors";
		assertLCVolLopped(id);
		assertNoUnloppedLCVol1(id);
		assertNoUnloppedLCVol2(id);
	}    

	/**
	 * lop when only diff is scheme DEWEY and DEWEYPER
	 */
@Test
	public void testLopDeweySchemeFlavors() 
	{
		String id = "deweyFlavors";
		assertDeweyVolLopped(id);
		assertNoUnloppedDeweyVol1(id);
		assertNoUnloppedDeweyVol2(id);
	} 

	/**
	 * lop when home location is different, but translates to same location
	 */
@Test
	public void testLopSameTranslLoc() 
	{
		String id = "sameTransLoc";
		assertLCVolLopped(id);
		assertNoUnloppedLCVol1(id);
		assertNoUnloppedLCVol2(id);
	}    

	/**
	 * lop some but not all item call numbers
	 */
@Test
	public void testLopSome() 
	{
		String id = "sameAndDiff";
		assertLCVol3NotLopped(id);
		assertLCVolLopped(id);
		assertNoUnloppedLCVol1(id);
		assertNoUnloppedLCVol2(id);
	}    

	/**
	 * only lop according to home location (not current location)
	 */
@Test
	public void testCurrLoc() 
	{
		String id = "diffCurrLoc";
		assertLCVol3NotLopped(id);
		assertLCVolLopped(id);
		assertNoUnloppedLCVol1(id);
		assertNoUnloppedLCVol2(id);
	}    

	/**
	 * skipped items should not factor into lopping
	 */
@Test
	public void testSkippedItems() 
	{
		String id = "skippedItem";
		assertLCVol2NotLopped(id);
		assertNoLoppedLC(id);
		// LC vol1 is in a skipped item
		assertNoUnloppedLCVol1(id);
	}    

	/**
	 * skipped call numbers should not factor into lopping
	 */
@Test
	public void testSkippedCallnums() 
	{
		String id = "skippedCallnum";
		assertLCVol1NotLopped(id);
		assertNoLoppedLC(id);
		// skipped call number should not be present
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, "NO CALL NUMBER");
	}    

	/**
	 * lop when scheme is incorrect
	 */
@Test
	public void testDeweyAsLC() 
	{
		String id = "deweyAsLC";
		assertDeweyVolLopped(id);
		assertNoUnloppedDeweyVol1(id);
		assertNoUnloppedDeweyVol2(id);
	} 
	
// --------- assert helper methods -----

	/**
	 * assert that the LC call number with Vol 1 used in many test records is not lopped
	 */
	private void assertLCVol1NotLopped(String id) 
	{
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, lcVol1Shelfkey);
	}    
	/**
	 * assert absence of unlopped LC Vol1 call number 
	 */
	private void assertNoUnloppedLCVol1(String id) 
	{
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, lcVol1Shelfkey);
	}    
	/**
	 * assert that the LC call number with Vol 2 used in many test records is not lopped
	 */
	private void assertLCVol2NotLopped(String id) 
	{
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, lcVol2Shelfkey);
	}    
	/**
	 * assert absence of unlopped LC Vol2 call number
	 */
	private void assertNoUnloppedLCVol2(String id) 
	{
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, lcVol2Shelfkey);
	}    
	/**
	 * assert that the LC call number with Vol 3 used in many test records is not lopped
	 */
	private void assertLCVol3NotLopped(String id) 
	{
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, lcVol3Shelfkey);
	}    
	
	/**
	 * assert that the LC call number with Vols used in many test records is lopped
	 */
	private void assertLCVolLopped(String id) 
	{
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, lcLoppedShelfkey);
	}    
	/**
	 * assert that the LC call number with Vols used in many test records is lopped
	 */
	private void assertNoLoppedLC(String id) 
	{
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, lcLoppedShelfkey);
	}    
	
	/**
	 * assert that the Dewey call number with Vol 1 used in many test records is not lopped
	 */
	private void assertDeweyVol1NotLopped(String id) 
	{
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, deweyVol1Shelfkey);
	}    
	/**
	 * assert absence of unlopped Dewey Vol1 call number
	 */
	private void assertNoUnloppedDeweyVol1(String id) 
	{
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, deweyVol1Shelfkey);
	}    
	/**
	 * assert absence of unlopped Dewey Vol2 call number
	 */
	private void assertNoUnloppedDeweyVol2(String id) 
	{
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, deweyVol2Shelfkey);
	}    
	/**
	 * assert that the Dewey call number with Vols used in many test records is lopped
	 */
	private void assertDeweyVolLopped(String id) 
	{
	    solrFldMapTest.assertSolrFldValue(testFilePath, id, fldName, deweyLoppedShelfkey);
	}    
	/**
	 * assert that the Dewey call number with Vols used in many test records is lopped
	 */
	private void assertNoLoppedDewey(String id) 
	{
	    solrFldMapTest.assertSolrFldHasNoValue(testFilePath, id, fldName, deweyLoppedShelfkey);
	}    

}
