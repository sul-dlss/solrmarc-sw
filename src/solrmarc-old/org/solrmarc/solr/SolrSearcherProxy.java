package org.solrmarc.solr;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.concurrent.Future;

public class SolrSearcherProxy
{
    Object solrSearcherObj = null;
    Object solrCoreObj = null;
    
    public SolrSearcherProxy(SolrCoreProxy solrCoreProxy)
    {
        solrCoreObj = solrCoreProxy.getCore();
        try
        {     
//          refedSolrSearcher = solrCore.getSearcher();
//          solrSearcher = refedSolrSearcher.get();
            Future waitSearcher[] = new Future[]{null};
            Object refCountedSolrSearcher = solrCoreObj.getClass().getMethod("getSearcher", boolean.class, boolean.class, waitSearcher.getClass()).
                                                    invoke(solrCoreObj, false, true, waitSearcher);
            solrSearcherObj = refCountedSolrSearcher.getClass().getMethod("get").invoke(refCountedSolrSearcher);
            if (waitSearcher[0] != null)
            {
                waitSearcher[0].get();
            }
        }
        catch (Exception e)
        {
            if (e instanceof java.lang.NoSuchMethodException)
            {
                Method methods[] = solrCoreObj.getClass().getMethods();
                for (Method method : methods)
                {
                    if (method.getName().equals("getSearcher"))
                    {
                        System.err.print(method.getName() + "(");
                        Class<?> classes[] = method.getParameterTypes();
                        for (Class clazz : classes)
                        {
                            System.err.print(clazz.getName() + ", ");                            
                        }
                        System.err.println(")");
                    }
                }
            }
            e.printStackTrace();
        }
    }

    public void close()
    {
        try 
        {
        	solrSearcherObj.getClass().getMethod("close").invoke(solrSearcherObj);
        	solrCoreObj.getClass().getMethod("closeSearcher").invoke(solrCoreObj);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        } 
        solrSearcherObj = null;
   }
    
    /**
     * get the user specified Solr doc ids for the DocSet returned when the 
     *  implied term query search is performed
     * @param field - the name of the field for the term query
     * @param value - the value of the field for the term query
     * @param docIdField - name of the field containing the user specified 
     *  unique key for the Solr document
     * @return array of Strings containing the unique keys of the Solr documents
     *  matching the query
     */
    public String[] getDocIdsFromSearch(String field, String value, String docIdField) throws IOException
    {
        String resultSet[] = null;
        try
        {
            Object docSet = getSolrDocSet(field, value);
            int totalSize = (Integer)docSet.getClass().getMethod("size").invoke(docSet);
            resultSet = new String[totalSize];
            //System. out.println("Searching for :" + field +" : "+ term+ "    Num found = " + totalSize);
            Object docIterator = docSet.getClass().getMethod("iterator").invoke(docSet);
            int i = 0;
            while (iteratorHasNext(docIterator))
            {
                int solrDocNum = iteratorGetNextSolrId(docIterator);
                resultSet[i++] = getDocIdFromSolrDocNum(solrDocNum, docIdField);
            }
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(resultSet);
    }
    
    /**
     * get the Solr doc numbers for the DocSet returned when the implied term query 
     *  search is performed
     * @param field - the name of the field for the term query
     * @param value - the value of the field for the term query
     * @return array of int containing the Solr doc numbers
     */
    public int[] getDocSet(String field, String value) throws IOException
    {
        int resultSet[] = null;
        try
        {
            Object docSet = getSolrDocSet(field, value);
            int totalSize = (Integer)docSet.getClass().getMethod("size").invoke(docSet);
            resultSet = new int[totalSize];
            //System. out.println("Searching for :" + field +" : "+ term+ "    Num found = " + totalSize);
            Object docIterator = docSet.getClass().getMethod("iterator").invoke(docSet);
            int i = 0;
            while (iteratorHasNext(docIterator))
            {
                resultSet[i++] = iteratorGetNextSolrId(docIterator);
            }
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(resultSet);
    }
    
    /**
	 * Given an index field name and value, return a list of Lucene Document
	 *  ids that match the term query sent to the index, sorted (by Lucene) by 
	 *  the indicated field (ASCENDING)
	 * @param fieldName - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sortfld - name of the field by which results should be sorted
	 * @return an array of int containing the sorted document ids
     */
    public int[] getAscSortDocNums(String fieldName, String value, String sortfld) throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException
    {
        try
        {
            Object sortObj = getSortObject(sortfld, false);
            Object docListObj = getSortedSolrDocList(fieldName, value, sortObj);
            return getIdsFromDocList(docListObj);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(null);    
    }
    
    /**
	 * Given an index field name and value, return a list of Lucene Document
	 *  ids that match the term query sent to the index, sorted (by Lucene) by 
	 *  the indicated field (DESCENDING)
	 * @param fieldName - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sortfld - name of the field by which results should be sorted
	 * @return an array of int containing the sorted document ids
     */
    public int[] getDescSortDocNums(String fieldName, String value, String sortfld) throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException
    {
        try
        {
            Object sortObj = getSortObject(sortfld, true);
            Object docListObj = getSortedSolrDocList(fieldName, value, sortObj);
            return getIdsFromDocList(docListObj);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(null);    
    }
    
    
    /**
     * Given an index field name and sort direction, return a corresponding Sort 
     *  object
     * @param fieldName
     * @param descending  true if sort is an descending sort
     * @return a Sort Object
     */
    private Object getSortObject(String fieldName, boolean descending)
    	throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException
    {
		Class solrIndexSearcherClass = Class.forName("org.apache.solr.search.SolrIndexSearcher");
        Object solrIndexSearchObj = solrIndexSearcherClass.cast(solrSearcherObj);

        Object indexSchemaObj = solrIndexSearcherClass.getMethod("getSchema").invoke(solrIndexSearchObj);
        Class indexSchemaClass = Class.forName("org.apache.solr.schema.IndexSchema");
        
        Object schemaFieldObj = indexSchemaClass.getMethod("getField", String.class).invoke(indexSchemaObj, fieldName);
        Class schemaFieldClass = Class.forName("org.apache.solr.schema.SchemaField");

		Object sortFieldObj = schemaFieldClass.getMethod("getSortField", boolean.class).invoke(schemaFieldObj, descending);
        Class sortFieldClass = Class.forName("org.apache.lucene.search.SortField");

        Class sortClass = Class.forName("org.apache.lucene.search.Sort");
        Object sortObj = sortClass.getConstructor(sortFieldClass).newInstance(sortFieldObj);
        return sortObj;
    }
    
    /**
     * Given an index field name and value, and a Sort object, return a DocList Object
     * matching the implied term query and lucene sort (lucene is doing the
     * sort, not this method)
     * 
     * @param fld - the name of the field to be searched in the lucene index
     * @param value - the string to be searched in the given field
     * @param sort - org.apache.lucene.search.Sort object
     * @return DocList of the matching sorted documents
     */
    private Object getSortedSolrDocList(String field, String value, Object sortObj) throws IOException, IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException
	{
        Class termClass = Class.forName("org.apache.lucene.index.Term");
        Object termObj = termClass.getConstructor(String.class, String.class).newInstance(field, value);
        Class termQueryClass = Class.forName("org.apache.lucene.search.TermQuery");
        Object termQueryObj = termQueryClass.getConstructor(termClass).newInstance(termObj);
        
		Class solrIndexSearcherClass = Class.forName("org.apache.solr.search.SolrIndexSearcher");
        Object solrIndexSearchObj = solrIndexSearcherClass.cast(solrSearcherObj);
        Class queryClass = Class.forName("org.apache.lucene.search.Query");
        Object queryObj = queryClass.cast(termQueryObj);
		Object docSetObj = solrIndexSearcherClass.getMethod("getDocSet", queryClass).invoke(solrIndexSearchObj, queryObj);
		
        Class docSetClass = Class.forName("org.apache.solr.search.DocSet");
        Class sortClass = Class.forName("org.apache.lucene.search.Sort");
        int docSetSize = (Integer) docSetClass.getMethod("size").invoke(docSetObj);
        Object docListObj = solrIndexSearcherClass.getMethod("getDocList", queryClass, docSetClass, sortClass, int.class, int.class).
                                        invoke(solrIndexSearchObj, queryObj, docSetObj, sortObj, 0, docSetSize);
		return docListObj;
	}

    
    /**
     * Given an (ordered) Solr docList Object, return the document ids as an array
     * of int, preserving order
     */
    private int[] getIdsFromDocList(Object docListObj)
        throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, ClassNotFoundException
    {
        int docSetSize = (Integer) docListObj.getClass().getMethod("size").invoke(docListObj);
        int[] resultSet = new int[docSetSize];

        Class docIterClass = Class.forName("org.apache.solr.search.DocIterator");
        Object docIterObj = docListObj.getClass().getMethod("iterator").invoke(docListObj);

        int i = 0;
        while ((Boolean) docIterClass.getMethod("hasNext").invoke(docIterObj))
        {
            resultSet[i++] = (Integer) docIterClass.getMethod("nextDoc").invoke(docIterObj);
        }

        return resultSet;    
    }
    

	
    /** needs comment */
    public Object getDocSetIterator(String field, String term) throws IOException
    {
        try
        {
            Object docSet = getSolrDocSet(field, term);
            Object docIterator = docSet.getClass().getMethod("iterator").invoke(docSet);
            return(docIterator);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(null);
    }


    /**
	 * Given an index field name and value, return the number of documents that
	 *  match the implied term query
	 * @param field - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
     */
    public int getNumberOfHits(String field, String value) throws IOException
    {
        try
        {
            Object docSetObj = getSolrDocSet(field, value);
            if (docSetObj != null)
                return (Integer)docSetObj.getClass().getMethod("size").invoke(docSetObj);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(-1);
    }

    
    /** needs comment */
    private Object getSolrDocSet(String field, String value) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException 
    {
        Object schema = solrSearcherObj.getClass().getMethod("getSchema").invoke(solrSearcherObj);
        Class queryClass = Class.forName("org.apache.lucene.search.Query");
        Class parser = Class.forName("org.apache.solr.search.QueryParsing");
        Object query = parser.getMethod("parseQuery", String.class, String.class, schema.getClass())
                             .invoke(null, value, field, schema);

        return solrSearcherObj.getClass().getMethod("getDocSet", queryClass).invoke(solrSearcherObj, query);
    }
    
    /** needs comment */
    public boolean iteratorHasNext(Object docSetIterator)
    {
        boolean result = false;
        try
        {
            result = (Boolean)(docSetIterator.getClass().getInterfaces()[0].getMethod("hasNext").invoke(docSetIterator));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return(result);
    }
    
    /** needs comment */
    public int iteratorGetNextSolrId(Object docSetIterator) throws IOException
    {
        int docNo = -1;
        try
        {
            docNo = (Integer)(docSetIterator.getClass().getInterfaces()[0].getMethod("next").invoke(docSetIterator));
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(docNo);
    }
    
    /** needs comment */
    public DocumentProxy iteratorGetNextDoc(Object docSetIterator) throws IOException
    {
        Object docProxyObj = null;
        try
        {
            int docNo = (Integer)(docSetIterator.getClass().getInterfaces()[0].getMethod("next").invoke(docSetIterator));
            docProxyObj = solrSearcherObj.getClass().getMethod("doc", int.class).invoke(solrSearcherObj, docNo);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(new DocumentProxy(docProxyObj));
    }
    
    
    /**
     * given a Solr document number, return the id of the Solr document, as 
     *  stored in the docIdField
     * @param docNo - Solr document number as an int
     * @param docIdField - name of the field containing the user specified 
     *  unique key for the Solr document
     * @return the value of the docIdField in the Solr document, as a String
     */
    public String getDocIdFromSolrDocNum(int docNo, String docIdField) throws IOException
    {
        String id = null;
        try
        {
            Class solrIndexSearcherClass = Class.forName("org.apache.solr.search.SolrIndexSearcher");
            Object solrIndexSearchObj = solrIndexSearcherClass.cast(solrSearcherObj);

            Object docObj = solrSearcherObj.getClass().getMethod("doc", int.class).invoke(solrSearcherObj, docNo);
            Object fieldObj = docObj.getClass().getMethod("getField", String.class).invoke(docObj, docIdField);
			if (fieldObj != null) 
                id = fieldObj.getClass().getMethod("stringValue").invoke(fieldObj).toString();
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(id);
    }
  
  
    /**
     * given a document number, get return the Solr document as a DocumentProxy 
     * Object
     * @param docNo - the Solr (internal) document number
     */
    public DocumentProxy getDocumentProxyBySolrDocNum(int docNo) throws IOException
    {
        Object docProxyObj = null;
        try
        {
            docProxyObj = solrSearcherObj.getClass().getMethod("doc", int.class).invoke(solrSearcherObj, docNo);
        }
        catch (Exception e)
        {
            if (e instanceof InvocationTargetException)
            {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException)cause;                
            }
            e.printStackTrace();
        }
        return(new DocumentProxy(docProxyObj));
    }
    
}