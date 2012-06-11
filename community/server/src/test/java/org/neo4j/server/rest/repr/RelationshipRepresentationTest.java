/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.rest.repr;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo4j.server.rest.repr.RepresentationTestAccess.serialize;
import static org.neo4j.server.rest.repr.RepresentationTestBase.NODE_URI_PATTERN;
import static org.neo4j.server.rest.repr.RepresentationTestBase.RELATIONSHIP_URI_PATTERN;
import static org.neo4j.server.rest.repr.RepresentationTestBase.assertUriMatches;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class RelationshipRepresentationTest
{
    @Test
    public void shouldHaveSelfLink() throws BadInputException
    {
        assertUriMatches( RELATIONSHIP_URI_PATTERN, relrep( 1234 ).selfUri() );
    }

    @Test
    public void shouldHaveType()
    {
        assertNotNull( relrep( 1234 ).getType() );
    }

    @Test
    public void shouldHaveStartNodeLink() throws BadInputException
    {
        assertUriMatches( NODE_URI_PATTERN, relrep( 1234 ).startNodeUri() );
    }

    @Test
    public void shouldHaveEndNodeLink() throws BadInputException
    {
        assertUriMatches( NODE_URI_PATTERN, relrep( 1234 ).endNodeUri() );
    }

    @Test
    public void shouldHavePropertiesLink() throws BadInputException
    {
        assertUriMatches( RELATIONSHIP_URI_PATTERN + "/properties", relrep( 1234 ).propertiesUri() );
    }

    @Test
    public void shouldHavePropertyLinkTemplate() throws BadInputException
    {
        assertUriMatches( RELATIONSHIP_URI_PATTERN + "/properties/\\{key\\}", relrep( 1234 ).propertyUriTemplate() );
    }

    @Test
    public void shouldSerialiseToMap()
    {
        Map<String, Object> repr = serialize( relrep( 1234 ) );
        assertNotNull( repr );
        verifySerialisation( repr );
    }

    private RelationshipRepresentation relrep( long id )
    {
        return new RelationshipRepresentation( relationship( id ) );
    }

    static Relationship relationship( long id )
    {
        Node startNode = mock( Node.class );
        when( startNode.getId() ).thenReturn( 0L );

        Node endNode = mock( Node.class );
        when( endNode.getId() ).thenReturn( 1L );

        RelationshipType type = mock( RelationshipType.class );
        when( type.name() ).thenReturn( "LOVES" );

        Relationship relationship = mock( Relationship.class );
        when( relationship.getId() ).thenReturn( id );
        when( relationship.getPropertyKeys() ).thenReturn( Collections.<String>emptySet() );
        when( relationship.getStartNode() ).thenReturn( startNode );
        when( relationship.getEndNode() ).thenReturn( endNode );
        when( relationship.getType() ).thenReturn( type );

        return relationship;
    }

    public static void verifySerialisation( Map<String, Object> relrep )
    {
        assertUriMatches( RELATIONSHIP_URI_PATTERN, relrep.get( "self" )
                .toString() );
        assertUriMatches( NODE_URI_PATTERN, relrep.get( "start" )
                .toString() );
        assertUriMatches( NODE_URI_PATTERN, relrep.get( "end" )
                .toString() );
        assertNotNull( relrep.get( "type" ) );
        assertUriMatches( RELATIONSHIP_URI_PATTERN + "/properties", relrep.get( "properties" )
                .toString() );
        assertUriMatches( RELATIONSHIP_URI_PATTERN + "/properties/\\{key\\}", (String) relrep.get( "property" ) );
        assertNotNull( relrep.get( "data" ) );
    }
}