package edu.stanford;

import java.io.*;

import org.junit.*;
import org.marc4j.marc.*;

import edu.stanford.enumValues.Format;
import edu.stanford.enumValues.FormatOld;


/**
 * junit4 tests for Stanford University format_main_ssim field
 * Database formats are tested separately in FormatDatabaseTests
 * Physical formats are tested separated in FormatPhysicalTests
 * @author Naomi Dushay
 */
public class FormatMainTests extends AbstractStanfordTest
{
	private final String testDataFname = "formatTests.mrc";
	String testFilePath = testDataParentPath + File.separator + testDataFname;
	private final String fldName = "format_main_ssim";
	MarcFactory factory = MarcFactory.newInstance();
	private ControlField cf008 = factory.newControlField("008");
	private ControlField cf006 = factory.newControlField("006");
	private DataField df956sfx = factory.newDataField("956", '4', '0');
	{
		df956sfx.addSubfield(factory.newSubfield('u', " http://library.stanford.edu/sfx?stuff"));
	}



@Before
	public final void setup()
	{
		mappingTestInit();
	}


	/**
	 * Audio Non-Music format tests
	 */
@Test
	public final void testAudioNonMusic()
	{
		String fldVal = Format.SOUND_RECORDING.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06i", fldName, fldVal);
	}

	/**
	 * Book format tests
	 *   includes monographic series
	 */
@Test
	public final void testBookFormat()
	{
		String fldVal = Format.BOOK.toString();

		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07m", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06t07a", fldName, fldVal);

		// monographic series
// FIXME:  temporary for format redo
//		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07s00821m", fldName, fldVal);
//		solrFldMapTest.assertSolrFldValue(testFilePath, "5987319", fldName, fldVal);
//		solrFldMapTest.assertSolrFldValue(testFilePath, "5598989", fldName, fldVal);
//		solrFldMapTest.assertSolrFldValue(testFilePath, "223344", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5666387", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "666", fldName, fldVal);

		// formerly believed to be monographic series
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821m", fldName, fldVal);
	}

	/**
	 * if a continuing monographic resource (a book series) has an SFX link,
	 * then it should be format Journal.
	 */
@Test
	public final void testBookSeriesAsJournal()
	{
		String journalFldVal = Format.JOURNAL_PERIODICAL.toString();
		String bookSeriesFldVal = Format.BOOK_SERIES.toString();
		// based on 9343812 - SFX link
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01937cas a2200433 a 4500"));
		cf008.setData("070207c20109999mauqr m o     0   a0eng c");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalFldVal);

		// based on 9138750 - no SFX link
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01750cas a2200409 a 4500"));
		cf008.setData("101213c20109999dcufr m bs   i0    0eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, bookSeriesFldVal);

		// monographic series without SFX links
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07s00821m", fldName, bookSeriesFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5987319", fldName, bookSeriesFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "5598989", fldName, bookSeriesFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "223344", fldName, bookSeriesFldVal);
	}

	/**
	 * Computer File format tests
	 */
@Test
	public final void testComputerFile()
	{
		String fldVal = Format.COMPUTER_FILE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06m00826u", fldName, fldVal);

		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("01529cmi a2200397Ia 4500"));
		cf008.setData("081215c200u9999xx         b        eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

	/**
	 * Computer File and Database -- if both formats, and it is only an
	 *  online resource, then it is NOT a computer file.
	 */
@Test
	public final void testDatabaseAndComputerFile()
	{
		Leader LEADER = factory.newLeader("02441cms a2200517 a 4500");
		cf008.setData("920901d19912002pauuu1n    m  0   a0eng  ");

		Record record = factory.newRecord();
		record.setLeader(LEADER);
		record.addVariableField(cf008);
		// online copy only
		DataField df999online = factory.newDataField("999", ' ', ' ');
		df999online.addSubfield(factory.newSubfield('a', "INTERNET RESOURCE"));
		df999online.addSubfield(factory.newSubfield('w', "ASIS"));
		df999online.addSubfield(factory.newSubfield('i', "2475606-5001"));
		df999online.addSubfield(factory.newSubfield('l', "INTERNET"));
		df999online.addSubfield(factory.newSubfield('m', "SUL"));
		df999online.addSubfield(factory.newSubfield('t', "DATABASE"));
		record.addVariableField(df999online);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.DATABASE_A_Z.toString());

		// both physical copy and online copy
		DataField df999physical = factory.newDataField("999", ' ', ' ');
		df999physical = factory.newDataField("999", ' ', ' ');
		df999physical.addSubfield(factory.newSubfield('a', "F152 .A28"));
		df999physical.addSubfield(factory.newSubfield('w', "LC"));
		df999physical.addSubfield(factory.newSubfield('i', "36105018746623"));
		df999physical.addSubfield(factory.newSubfield('l', "HAS-DIGIT"));
		df999physical.addSubfield(factory.newSubfield('m', "GREEN"));
		record.addVariableField(df999physical);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 2);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.COMPUTER_FILE.toString());
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.DATABASE_A_Z.toString());

		// can't have physical copy only or it wouldn't be a database
	}


	/**
	 * Conference Proceedings format tests - Conf Proc is now a Genre
	 */
@Test
	public final void testConferenceProceedingsIsGone()
	{
	    String fldVal = FormatOld.CONFERENCE_PROCEEDINGS.toString();
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "5666387", fldName, fldVal);
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "666", fldName, fldVal);
	}

	/**
	 * test format population of Datasets
	 */
@Test
	public final void testDataset()
	{
		String fldVal = Format.DATASET.toString();

		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06m00826a", fldName, fldVal);

		Leader LEADER = factory.newLeader("01529cmi a2200397Ia 4500");
		Record record = factory.newRecord();
		record.setLeader(LEADER);
		cf008.setData("081215c200u9999xx         a        eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}


	/**
	 * Image format tests
	 */
@Test
	public final void testImage()
	{
		String fldVal = Format.IMAGE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833i", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833k", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833p", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833s", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833t", fldName, fldVal);
	}

	/**
	 * Journal/Periodical format tests
	 */
@Test
	public final void testJournalPeriodicalFormat()
	{
        String fldVal = Format.JOURNAL_PERIODICAL.toString();

     	// leader/07 s 008/21 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06a07s", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "4114632", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "123", fldName, fldVal);
		// 006/00 s /04 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821m", fldName, fldVal);
		// 006/00 s /04 blank
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821p", fldName, fldVal);
		// even though LCPER in 999 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "460947", fldName, fldVal);
		// even though DEWEYPER in 999 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "446688", fldName, fldVal);

		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821p", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "335577", fldName, fldVal);

		// leader/07s 008/21 d   006/00 s  006/04 d -- other
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "112233", fldName, fldVal);

		// leader/07 s, 006/00 m, 008/21 |  -- we are favoring anything in 008/21  over  006/00
		solrFldMapTest.assertSolrFldValue(testFilePath, "7117119", fldName, fldVal);


		// No 006
		// 008 byte 21 is p  (Journal / periodical)
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("02808cas a22005778a 4500"));
		cf008.setData("050127c20149999enkfr p       |   a0eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);

		// 008 byte 21 is blank
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02393cas a2200421Ki 4500"));
		cf008.setData("130923c20139999un uu         1    0ukr d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);

		// 008 byte 21 is | (pipe)  Journal
		record = factory.newRecord();
		record.setLeader(factory.newLeader("00756nas a22002175a 4500"));
		cf008.setData("110417s2011    le |||||||||||||| ||ara d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);

		// have 006

		// 006 byte 4 is p
		record = factory.newRecord();
		record.setLeader(factory.newLeader("03163cas a2200553 a 4500"));
		cf006.setData("ser p       0    0");
		record.addVariableField(cf006);
		cf008.setData("000000d197819uuilunnn         l    eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);

		// 006 byte 4 is blank
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02393cas a2200421Ki 4500"));
		cf008.setData("130923c20139999un uu         1    0ukr d");
		record.addVariableField(cf008);
		cf006.setData("ser         0    0");
		record.addVariableField(cf006);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);

		// 006 byte 4 is pipe
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02393cas a2200421Ki 4500"));
		cf008.setData("130923c20139999un uu         1    0ukr d");
		record.addVariableField(cf008);
		cf006.setData("suu wss|||||0   |2");
		record.addVariableField(cf006);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);

// FIXME:  Not sure what to do with these double formats
//		// recording and journal
//		record = factory.newRecord();
//		record.setLeader(factory.newLeader("03163cis a2200553 a 4500"));
//		cf006.setData("ser p       0    0");
//		record.addVariableField(cf006);
//		cf008.setData("000000d197819uuilunnn         l    eng d");
//		record.addVariableField(cf008);
////		solrFldMapTest.assertSolrFldValue(record, fldName, Format.JOURNAL_PERIODICAL.toString());
//
//		// recording and conf proceedings
//		record = factory.newRecord();
//		record.setLeader(factory.newLeader("03701cim a2200421 a 4500"));
//		cf006.setData("sar         1    0");
//		record.addVariableField(cf006);
//		cf008.setData("040802c200u9999cau            l    eng d");
//		record.addVariableField(cf008);
////		solrFldMapTest.assertSolrFldValue(record, fldName, Format.JOURNAL_PERIODICAL.toString());
//
//		// score and database and journal
//		record = factory.newRecord();
//		record.setLeader(factory.newLeader("02081cci a2200385 a 4500"));
//		cf006.setData("m        d        ");
//		record.addVariableField(cf006);
//		cf006.setData("suu wss|||||0   |2");
//		record.addVariableField(cf006);
//		cf008.setData("050921c20039999iluuus ss0     n   2eng  ");
//		record.addVariableField(cf008);
////		solrFldMapTest.assertSolrFldValue(record, fldName, Format.JOURNAL_PERIODICAL.toString());
	}



	/**
	 * Manuscript/Archive format tests
	 */
@Test
	public final void testManuscriptArchive()
	{
		String fldVal = Format.MANUSCRIPT_ARCHIVE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06b", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06p", fldName, fldVal);
	}

	/**
	 * Map/Globe format tests
	 */
@Test
	public final void testMapGlobe()
	{
		String fldVal = Format.MAP.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06e", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06f", fldName, fldVal);
	}

	/**
	 * test format of Journal for Marcit source records (per 590)
	 */
@Test
	public final void testMarcit()
	{
		String fldVal = Format.JOURNAL_PERIODICAL.toString();

		Leader LEADER = factory.newLeader("00838cas a2200193z  4500");

		Record record = factory.newRecord();
		record.setLeader(LEADER);
		DataField df = factory.newDataField("590", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "MARCit brief record."));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
		// without period
		record = factory.newRecord();
		record.setLeader(LEADER);
		df = factory.newDataField("590", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "MARCit brief record"));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);

		// wrong string in 590
		record = factory.newRecord();
		record.setLeader(LEADER);
		df = factory.newDataField("590", ' ', ' ');
		df.addSubfield(factory.newSubfield('a', "incorrect string"));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.OTHER.toString());
		record = factory.newRecord();
		record.setLeader(LEADER);
		df = factory.newDataField("590", ' ', ' ');
		df.addSubfield(factory.newSubfield('a', "something MARCit something"));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.OTHER.toString());

		// marcit in wrong field
		record = factory.newRecord();
		record.setLeader(LEADER);
		df = factory.newDataField("580", ' ', ' ');
        df.addSubfield(factory.newSubfield('a', "MARCit brief record."));
        record.addVariableField(df);
		solrFldMapTest.assertSolrFldValue(record, fldName, Format.OTHER.toString());
	}


	/**
	 * Microformat format tests - Microformats are now Physical Format types
	 */
@Test
	public final void testMicroformatIsGone()
	{
		String fldVal = FormatOld.MICROFORMAT.toString();
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "245hmicroform", fldName, fldVal);

		String testFilePath = testDataParentPath + File.separator + "callNumberTests.mrc";
		// 999 ALPHANUM starting with MFLIM
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "1261173", fldName, fldVal);
		// 999 ALPHANUM starting with MFICHE
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "mfiche", fldName, fldVal);
	}

	/**
	 * Music Recording format tests
	 */
@Test
	public final void testMusicRecording()
	{
		String fldVal = Format.MUSIC_RECORDING.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06j", fldName, fldVal);
	}

	/**
	 * Music Score format tests
	 */
@Test
	public final void testMusicScore()
	{
		String fldVal = Format.MUSIC_SCORE.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06c", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06d", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "245hmicroform", fldName, fldVal);
	}

	/**
	 * Newspaper format tests
	 */
@Test
	public final void testNewspaper()
	{
        String fldVal = Format.NEWSPAPER.toString();

		solrFldMapTest.assertSolrFldValue(testFilePath, "newspaper", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821n", fldName, fldVal);
// FIXME:  presumably has 008 blank and 006 with byte 004 of n ...
// we are favoring anything in 008/21  over  006/00
//		solrFldMapTest.assertSolrFldValue(testFilePath, "334455", fldName, fldVal);

		// leader/07b 006/00s 008/21n - serial publication
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "leader07b00600s00821n", fldName, fldVal);

// FIXME:  we are checking to see if there are many differences between the old Journal/Newspaper algorithm and the new ...
// also,  we are favoring anything in 008/21  over  006/00
//		// 006 byte 4 is p
//		Record record = factory.newRecord();
//		record.setLeader(factory.newLeader("03163cas a2200553 a 4500"));
//		cf006.setData("ser n       0    0");
//		record.addVariableField(cf006);
//		cf008.setData("000000d197819uuilunnn         l    eng d");
//		record.addVariableField(cf008);
//		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

	/**
	 * Thesis format tests - Thesis is now a Genre
	 */
@Test
	public final void testThesisIsGone()
	{
		String fldVal = FormatOld.THESIS.toString();
		solrFldMapTest.assertSolrFldHasNoValue(testFilePath, "502", fldName, fldVal);
	}

	/**
	 * Updating Database can be a serial or integrating resource.
	 * If they have an SFX url, then we will call them a journal.
	 */
@Test
	public final void testUpdatingDatabase()
	{
		String journalFldVal = Format.JOURNAL_PERIODICAL.toString();
		String udbFldVal = Format.UPDATING_DATABASE.toString();

		// based on 9366507 - integrating, SFX
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("02018cai a2200397Ia 4500"));
		cf008.setData("120203c20089999enkwr d ob    0    2eng d");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalFldVal);

		// based on 6735313 - integrating, no SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01622cai a2200397 a 4500"));
		cf008.setData("061227c20069999vau x dss    f0    2eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, udbFldVal);

		// based on 8774277 - serial, SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02056cas a2200445Ii 4500"));
		cf008.setData("101110c20099999nz ar d o    f0    0eng d");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalFldVal);

		// serial, no SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01548cas a2200361Ia 4500"));
		cf008.setData("061227c20069999vau x dss    f0    2eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, udbFldVal);
	}

	/**
	 * Updating Looseleaf can be a serial or integrating resource.
	 * Both should be assigned to format Book
	 */
@Test
	public final void testUpdatingLooseleaf()
	{
		String fldVal = Format.BOOK.toString();
		// based on 7911837 - integrating
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("02444cai a2200433 a 4500"));
		cf008.setData("090205c20089999nyuuu l   b   0   a2eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);

		// serial
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02444cas a2200433 a 4500"));
		cf008.setData("090205c20089999nyuuu l   b   0   a2eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, fldVal);
	}

	/**
	 * Updating Website can be a serial or integrating resource.
	 * If they have an SFX url, then we will call them a journal.
	 */
@Test
	public final void testUpdatingWebsite()
	{
		String journalFldVal = Format.JOURNAL_PERIODICAL.toString();
		String uwebFldVal = Format.UPDATING_WEBSITE.toString();

		// based on 10094805 - integrating, SFX
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("02015cai a2200385 a 4500"));
		cf008.setData("130110c20139999enk|| woo     0    2eng  ");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalFldVal);

		// based on 8541457 - integrating, no SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01548cai a2200361Ia 4500"));
		cf008.setData("040730d19uu2012dcuar w os   f0    2eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, uwebFldVal);

		// serial, SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02015cas a2200385 a 4500"));
		cf008.setData("130110c20139999enk|| woo     0    2eng  ");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalFldVal);

		// serial, no SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01548cas a2200361Ia 4500"));
		cf008.setData("040730d19uu2012dcuar w os   f0    2eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, uwebFldVal);

		// 006/00 s /04 w
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821n", fldName, uwebFldVal);
		// web site
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821w", fldName, uwebFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821w", fldName, uwebFldVal);
	}

	/**
	 * Updating Other can be a serial or integrating resource.
	 * If they have an SFX url, then we will call them a journal.
	 */
@Test
	public final void testUpdatingOther()
	{
		String journalFldVal = Format.JOURNAL_PERIODICAL.toString();
		String uOtherFldVal = Format.UPDATING_OTHER.toString();

		// based on 9539608 - integrating, SFX
		Record record = factory.newRecord();
		record.setLeader(factory.newLeader("02085cai a2200325 a 4500"));
		cf008.setData("111014c20119999enk|| p o     |    2eng c");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalFldVal);

		// based on 10182766k - integrating, no SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("01579cai a2200337Ia 4500"));
		cf008.setData("081215c200u9999xx         a        eng d");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, uOtherFldVal);

		// serial, SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02085cas a2200325 a 4500"));
		cf008.setData("111014c20119999enk|| p o     |    2eng c");
		record.addVariableField(cf008);
		record.addVariableField(df956sfx);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalFldVal);

		// serial, no SFX
		record = factory.newRecord();
		record.setLeader(factory.newLeader("02085cas a2200325 a 4500"));
		cf008.setData("111014c20119999enk|| p o     |    2eng c");
		record.addVariableField(cf008);
		solrFldMapTest.assertSolrFldValue(record, fldName, journalFldVal);
	}

	/**
	 * Video format tests
	 */
@Test
	public final void testVideo()
	{
		String fldVal = Format.VIDEO.toString();
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833m", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833v", fldName, fldVal);
	}

	/**
	 * Test assignment of Other format
	 */
@Test
	public final void testOtherFormat()
	{
        String fldVal = Format.OTHER.toString();

		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06t07b", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06k00833w", fldName, fldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06g00833w", fldName, fldVal);
		// 006/00 s /04 w
// FIXME:  temporary for format redo
//		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821n", fldName, fldVal);
		// instructional kit
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06o", fldName, fldVal);
		// object
		solrFldMapTest.assertSolrFldValue(testFilePath, "leader06r", fldName, fldVal);
		// web site
//		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07sNo00600821w", fldName, fldVal);
//		solrFldMapTest.assertSolrFldValue(testFilePath, "leader07b00600s00821w", fldName, fldVal);
	}


	/**
	 * test format population based on ALPHANUM field values from 999
	 */
@Test
	public final void testFormatsFrom999()
	{
		String testFilePath = testDataParentPath + File.separator + "callNumberTests.mrc";

		// 999 ALPHANUM starting with MCD
// currently not using callnums for MCD, or DVD
//		solrFldMapTest.assertSolrFldValue(testFilePath, "1234673", fldName, Format.MUSIC_RECORDING.toString());
	}

}
