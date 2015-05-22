package edu.stanford;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University's managed Purls from StanfordSync
 * @author Laney McGlohon
 */
public class ManagedPurlTests extends AbstractStanfordTest 
{
	private final String testDataFname = "ManagedPurlTests.xml";

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		createFreshIx(testDataFname);
	}

	/**
	 * test managed_purl_urls field
	 */
@Test
	public final void testManagedPurlUrls() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "managed_purl_urls";
			
		assertDocHasFieldValue("managedPurlItem1Collection", fldName, "http://purl.stanford.edu/nz353cp1092"); 
		assertDocHasFieldValue("managedPurlItem3Collections", fldName, "http://purl.stanford.edu/wd297xz1362"); 
		assertDocHasFieldValue("managedPurlCollection", fldName, "http://purl.stanford.edu/ct961sj2730"); 
		assertDocHasFieldValue("ManagedAnd2UnmanagedPurlCollection", fldName, "http://purl.stanford.edu/ct961sj2730"); 
		
		assertDocHasNoField("NoManagedPurlItem", fldName);
		assertDocHasNoFieldValue("ManagedAnd2UnmanagedPurlCollection", fldName, "http://purl.stanford.edu/yy000zz0000");
	}
	
	/**
	 * Test method for display_type.
	 */
@Test
	public final void testDisplayType() throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "display_type";
	
		assertDocHasFieldValue("managedPurlItem3Collections", fldName, "citation"); 
		assertDocHasFieldValue("ManagedAnd2UnmanagedPurlCollection", fldName, "image"); 
		
		assertDocHasNoField("NoManagedPurlItem", fldName);
		assertDocHasNoFieldValue("ManagedAnd2UnmanagedPurlCollection", fldName, "book");
	}
	

	/**
	 * Test collection field contents
	 */
@Test
	public final void testCollection() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "collection";
			
		assertDocHasFieldValue("managedPurlItem1Collection", fldName, "yg867hg1375"); 
		assertDocHasFieldValue("amanagedPurlItem3Collections", fldName, "yg867hg1375, aa000bb111, yy000zz0000"); 
		assertDocHasNoField("ManagedAnd2UnmanagedPurlCollection", fldName); 
		assertDocHasNoField("NoManagedPurlItem", fldName); 
	}

	/**
	 * Test collection_with_title field contents
	 */
@Test
	public final void testCollectionWithTitle() 
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		String fldName = "collection_with_title";
			
		assertDocHasFieldValue("managedPurlItem1Collection", fldName, "[yg867hg1375-|-Francis E. Stafford photographs, 1909-1933]"); 
		assertDocHasFieldValue("amanagedPurlItem3Collections", fldName, "[yg867hg1375-|-Francis E. Stafford photographs, 1909-1933, aa000bb1111-|-Test Collection, 1963-2015, yy000zz1111-|-Test Collection2, 1968-2015]"); 

		assertDocHasNoField("managedPurlCollection", fldName);
		assertDocHasNoField("ManagedAnd2UnmanagedPurlCollection", fldName);
		assertDocHasNoField("NoManagedPurlItem", fldName); 
	}


	/**
	 * test collection_type
	 */
@Test
	public final void testCollectionType() 
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		String fldName = "collection_type";
		assertDocHasFieldValue("managedPurlCollection", fldName, "Digital Collection"); 
		assertDocHasFieldValue("ManagedAnd2UnmanagedPurlCollection", fldName, "Digital Collection"); 
		assertDocHasNoField("NoManagedPurlItem", fldName); 
		assertDocHasNoField("managedPurlItem1Collection", fldName);
		assertDocHasNoField("managedPurlItem3Collections", fldName);
	}
}
