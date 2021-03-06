package org.hisp.dhis.jdbc;

/*
 * Copyright (c) 2004-2015, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.List;

import org.hisp.dhis.period.Period;

/**
 * @author Lars Helge Overland
 * @version $Id: StatementBuilder.java 5715 2008-09-17 14:05:28Z larshelg $
 */
public interface StatementBuilder
{
    final String QUOTE = "'";

    //--------------------------------------------------------------------------
    // General
    //--------------------------------------------------------------------------
    
    /**
     * Encodes the provided SQL value. Value will be wrapped in quotes.
     * 
     * @param value the value.
     * @return the SQL encoded value.
     */
    String encode( String value );

    /**
     * Encodes the provided SQL value.
     * 
     * @param value the value.
     * @param quote whether to wrap the resulting value in quotes.
     * @return the SQL encoded value.
     */
    String encode( String value, boolean quote );
    
    /**
     * Returns the character used to quote database table and column names.
     * 
     * @return a quote character.
     */
    String getColumnQuote();
    
    /**
     * Wraps the given column or table in quotes.
     * 
     * @param column the column or table name.
     * @return the column or table name wrapped in quotes.
     */
    String columnQuote( String column );
    
    /**
     * Returns a limit and offset clause.
     * 
     * @param offset the offset / start position for the records to return.
     * @param limit the limit on max number of records to return.
     * @return a limit and offset clause.
     */
    String limitRecord( int offset, int limit );
    
    /**
     * Returns the value to use in insert statements for auto-increment columns.
     * @return value to use in insert statements for auto-increment columns.
     */
    String getAutoIncrementValue();
    
    /**
     * Returns statement for vacuum and analyze operations for a table. Returns
     * null if such statement is not relevant.
     * 
     * @param table the table to vacuum.
     * @return vacuum and analyze operations for a table.
     */
    String getVacuum( String table );
    
    /**
     * Returns a sql statement to include in create table statements with applies
     * options to the table. Returns an empty string if all options are set to the
     * default value.
     * 
     * @param autoVacuum whether to enable automatic vacuum, default is true.
     * @return a sql option string.
     */
    String getTableOptions( boolean autoVacuum );
    
    /**
     * Returns the name of a double column type.
     */
    String getDoubleColumnType();

    /**
     * Returns the name of a longvar column type.
     */
    String getLongVarBinaryType();

    /**
     * Returns the value used to match a column to a regular expression. Matching
     * is case insensitive.
     */
    String getRegexpMatch();

    /**
     * Returns the regular expression marker for end of a word.
     */
    String getRegexpWordStart();
    
    /**
     * Returns the regular expression marker for start of a word.
     */
    String getRegexpWordEnd();
    
    /**
     * Creates a SELECT statement returning the identifier of the given Period.
     * 
     * @param period the Period to use in the statement. 
     * @return a SELECT statement returning the identifier of the given Period.
     */
    String getPeriodIdentifierStatement( Period period );
    
    /**
     * Returns the number of columns part of the primary key for the given table.
     */
    String getNumberOfColumnsInPrimaryKey( String table );
    
    /**
     * Returns a cast to timestamp statement for the given column.
     * 
     * @param column the column name.
     * @return a cast to timestamp statement for the given column.
     */
    String getCastToDate( String column );
    
    /**
     * Creates a delete datavalue statement.
     * @return a delete datavalue statement.
     */
    String getDeleteZeroDataValues();
    
    String getMoveDataValueToDestination( int sourceId, int destinationId );

    String getSummarizeDestinationAndSourceWhereMatching( int sourceId, int destinationId );

    String getMoveFromSourceToDestination( int destDataElementId, int destCategoryOptionComboId,
        int sourceDataElementId, int sourceCategoryOptionComboId );

    String getUpdateDestination( int destDataElementId, int destCategoryOptionComboId,
        int sourceDataElementId, int sourceCategoryOptionComboId );
    
    String getStandardDeviation( int dataElementId, int categoryOptionComboId, int organisationUnitId );
    
    String getAverage( int dataElementId, int categoryOptionComboId, int organisationUnitId );
    
    String getAddDate( String dateField, int days );
    
    String queryDataElementStructureForOrgUnit();

    String queryRawDataElementsForOrgUnitBetweenPeriods( Integer orgUnitId, List<Integer> betweenPeriodIds );
    
    String getDropPrimaryKey( String table );
    
    String getAddPrimaryKeyToExistingTable( String table, String column );
    
    String getDropNotNullConstraint( String table, String column, String type );
}
