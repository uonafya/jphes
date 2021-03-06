package org.hisp.dhis.common;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hisp.dhis.common.NameableObject.NameableProperty;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.common.exception.InvalidIdentifierReferenceException;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.user.UserCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Note that it is required for nameable object stores to have concrete implementation
 * classes, not rely on the HibernateIdentifiableObjectStore class, in order to 
 * be injected as nameable object stores.
 * 
 * @author Lars Helge Overland
 */
@Transactional
public class DefaultIdentifiableObjectManager
    implements IdentifiableObjectManager
{
    private static final Log log = LogFactory.getLog( DefaultIdentifiableObjectManager.class );

    @Autowired
    private Set<GenericIdentifiableObjectStore<? extends IdentifiableObject>> identifiableObjectStores;

    @Autowired
    private Set<GenericNameableObjectStore<? extends NameableObject>> nameableObjectStores;

    @Autowired
    private Set<GenericDimensionalObjectStore<? extends DimensionalObject>> dimensionalObjectStores;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private OrganisationUnitService organisationUnitService;

    private Map<Class<? extends IdentifiableObject>, GenericIdentifiableObjectStore<? extends IdentifiableObject>> identifiableObjectStoreMap;

    private Map<Class<? extends NameableObject>, GenericNameableObjectStore<? extends NameableObject>> nameableObjectStoreMap;

    private Map<Class<? extends DimensionalObject>, GenericDimensionalObjectStore<? extends DimensionalObject>> dimensionalObjectStoreMap;

    //--------------------------------------------------------------------------
    // IdentifiableObjectManager implementation
    //--------------------------------------------------------------------------

    @Override
    public void save( IdentifiableObject object )
    {
        save( object, true );
    }

    @Override
    public void save( IdentifiableObject object, boolean clearSharing )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( object.getClass() );

        if ( store != null )
        {
            store.save( object, clearSharing );
        }
    }

    @Override
    public void update( IdentifiableObject object )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( object.getClass() );

        if ( store != null )
        {
            store.update( object );
        }
    }

    @Override
    public void update( List<IdentifiableObject> objects )
    {
        if ( objects == null || objects.isEmpty() )
        {
            return;
        }

        for ( IdentifiableObject object : objects )
        {
            update( object );
        }
    }

    @Override
    public void delete( IdentifiableObject object )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( object.getClass() );

        if ( store != null )
        {
            store.delete( object );
        }
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> T get( String uid )
    {
        for ( GenericIdentifiableObjectStore<? extends IdentifiableObject> store : identifiableObjectStores )
        {
            T object = (T) store.getByUid( uid );

            if ( object != null )
            {
                return object;
            }
        }

        return null;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> T get( Class<T> clazz, int id )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return null;
        }

        return (T) store.get( id );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> T get( Class<T> clazz, String uid )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return null;
        }

        return (T) store.getByUid( uid );
    }

    @Override
    public <T extends IdentifiableObject> boolean exists( Class<T> clazz, String uid )
    {
        return get( clazz, uid ) != null;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> T get( Collection<Class<? extends IdentifiableObject>> classes, String uid )
    {        
        for ( Class<? extends IdentifiableObject> clazz : classes )
        {
            T object = (T) get( clazz, uid );
            
            if ( object != null )
            {
                return object;
            }
        }
        
        return null;
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> T getByCode( Class<T> clazz, String code )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return null;
        }

        return (T) store.getByCode( code );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> T getByName( Class<T> clazz, String name )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return null;
        }

        return (T) store.getByName( name );
    }

    @Override
    public <T extends IdentifiableObject> T search( Class<T> clazz, String query )
    {
        T object = get( clazz, query );

        if ( object == null )
        {
            object = getByCode( clazz, query );
        }

        if ( object == null )
        {
            object = getByName( clazz, query );
        }

        return object;
    }

    @Override
    public <T extends IdentifiableObject> List<T> filter( Class<T> clazz, String query )
    {
        Set<T> uniqueObjects = new HashSet<>();

        T uidObject = get( clazz, query );

        if ( uidObject != null )
        {
            uniqueObjects.add( uidObject );
        }

        T codeObject = getByCode( clazz, query );

        if ( codeObject != null )
        {
            uniqueObjects.add( codeObject );
        }

        uniqueObjects.addAll( getLikeName( clazz, query ) );

        List<T> objects = new ArrayList<>( uniqueObjects );

        Collections.sort( objects, IdentifiableObjectNameComparator.INSTANCE );

        return objects;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getAll( Class<T> clazz )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAll();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getAllByName( Class<T> clazz, String name )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllEqName( name );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getAllByNameIgnoreCase( Class<T> clazz, String name )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllEqNameIgnoreCase( name );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getAllSorted( Class<T> clazz )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllOrderedName();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getAllSortedByLastUpdated( Class<T> clazz )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllOrderedLastUpdated();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getByUid( Class<T> clazz, Collection<String> uids )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getByUid( uids );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getByUidOrdered( Class<T> clazz, List<String> uids )
    {
        GenericIdentifiableObjectStore<T> store = (GenericIdentifiableObjectStore<T>) getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>();

        if ( uids != null )
        {
            for ( String uid : uids )
            {
                T object = store.getByUid( uid );

                if ( object != null )
                {
                    list.add( object );
                }
            }
        }

        return list;
    }

    @Override
    public <T extends IdentifiableObject> int getCount( Class<T> clazz )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store != null )
        {
            return store.getCount();
        }

        return 0;
    }

    @Override
    public <T extends IdentifiableObject> int getCountByName( Class<T> clazz, String name )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store != null )
        {
            return store.getCountEqName( name );
        }

        return 0;
    }

    @Override
    public <T extends NameableObject> int getCountByShortName( Class<T> clazz, String shortName )
    {
        GenericNameableObjectStore<NameableObject> store = getNameableObjectStore( clazz );

        if ( store != null )
        {
            return store.getCountEqShortName( shortName );
        }

        return 0;
    }

    @Override
    public <T extends IdentifiableObject> int getCountByCreated( Class<T> clazz, Date created )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store != null )
        {
            return store.getCountGeCreated( created );
        }

        return 0;
    }

    @Override
    public <T extends IdentifiableObject> int getCountByLastUpdated( Class<T> clazz, Date lastUpdated )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store != null )
        {
            return store.getCountGeLastUpdated( lastUpdated );
        }

        return 0;
    }

    @Override
    public <T extends IdentifiableObject> int getCountLikeName( Class<T> clazz, String name )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store != null )
        {
            return store.getCountLikeName( name );
        }

        return 0;
    }

    @Override
    public <T extends NameableObject> int getCountLikeShortName( Class<T> clazz, String shortName )
    {
        GenericNameableObjectStore<NameableObject> store = getNameableObjectStore( clazz );

        if ( store != null )
        {
            return store.getCountLikeShortName( shortName );
        }

        return 0;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getLikeName( Class<T> clazz, String name )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllLikeName( name );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends NameableObject> List<T> getLikeShortName( Class<T> clazz, String shortName )
    {
        GenericNameableObjectStore<NameableObject> store = getNameableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllLikeShortName( shortName );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getBetween( Class<T> clazz, int first, int max )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAll( first, max );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getBetweenSorted( Class<T> clazz, int first, int max )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllOrderedName( first, max );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getBetweenLikeName( Class<T> clazz, String name, int first, int max )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllLikeName( name, first, max );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getBetweenLikeName( Class<T> clazz, Set<String> words, int first, int max )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllLikeName( words, first, max );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getByLastUpdated( Class<T> clazz, Date lastUpdated )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllGeLastUpdated( lastUpdated );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getByCreated( Class<T> clazz, Date created )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllGeCreated( created );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getByLastUpdatedSorted( Class<T> clazz, Date lastUpdated )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllGeLastUpdatedOrderedName( lastUpdated );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getByCreatedSorted( Class<T> clazz, Date created )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllGeCreatedOrderedName( created );
    }

    @Override
    public <T extends IdentifiableObject> Date getLastUpdated( Class<T> clazz )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return null;
        }

        return store.getLastUpdated();
    }

    @Override
    public <T extends IdentifiableObject> Set<Integer> convertToId( Class<T> clazz, Collection<String> uids )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        Set<Integer> ids = new HashSet<>();

        for ( String uid : uids )
        {
            IdentifiableObject object = store.getByUid( uid );

            if ( object != null )
            {
                ids.add( object.getId() );
            }
        }

        return ids;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> Map<String, T> getIdMap( Class<T> clazz, IdentifiableProperty property )
    {
        Map<String, T> map = new HashMap<>();

        GenericIdentifiableObjectStore<T> store = (GenericIdentifiableObjectStore<T>) getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return map;
        }

        List<T> objects = store.getAll();
        
        return IdentifiableObjectUtils.getMap( objects, property );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> Map<String, T> getIdMapNoAcl( Class<T> clazz, IdentifiableProperty property )
    {
        Map<String, T> map = new HashMap<>();

        GenericIdentifiableObjectStore<T> store = (GenericIdentifiableObjectStore<T>) getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return map;
        }

        List<T> objects = store.getAllNoAcl();

        return IdentifiableObjectUtils.getMap( objects, property );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends NameableObject> Map<String, T> getIdMap( Class<T> clazz, NameableProperty property )
    {
        Map<String, T> map = new HashMap<>();

        GenericNameableObjectStore<T> store = (GenericNameableObjectStore<T>) getNameableObjectStore( clazz );

        List<T> objects = store.getAll();

        for ( T object : objects )
        {
            if ( property == NameableProperty.SHORT_NAME )
            {
                if ( object.getShortName() != null )
                {
                    map.put( object.getShortName(), object );
                }
            }
        }

        return map;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends NameableObject> Map<String, T> getIdMapNoAcl( Class<T> clazz, NameableProperty property )
    {
        Map<String, T> map = new HashMap<>();

        GenericNameableObjectStore<T> store = (GenericNameableObjectStore<T>) getNameableObjectStore( clazz );

        List<T> objects = store.getAllNoAcl();

        for ( T object : objects )
        {
            if ( property == NameableProperty.SHORT_NAME )
            {
                if ( object.getShortName() != null )
                {
                    map.put( object.getShortName(), object );
                }
            }
        }

        return map;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getObjects( Class<T> clazz, IdentifiableProperty property, Collection<String> identifiers )
    {
        GenericIdentifiableObjectStore<T> store = (GenericIdentifiableObjectStore<T>) getIdentifiableObjectStore( clazz );

        if ( identifiers != null && !identifiers.isEmpty() )
        {
            if ( property == null || IdentifiableProperty.UID.equals( property ) )
            {
                return store.getByUid( identifiers );
            }
            else if ( IdentifiableProperty.CODE.equals( property ) )
            {
                return store.getByCode( identifiers );
            }
            else if ( IdentifiableProperty.NAME.equals( property ) )
            {
                return store.getByName( identifiers );
            }
            
            throw new InvalidIdentifierReferenceException( "Invalid identifiable property / class combination: " + property );
        }

        return new ArrayList<>();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getObjects( Class<T> clazz, Collection<Integer> identifiers )
    {
        GenericIdentifiableObjectStore<T> store = (GenericIdentifiableObjectStore<T>) getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return store.getById( identifiers );
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> T getObject( Class<T> clazz, IdentifiableProperty property, String id )
    {
        GenericIdentifiableObjectStore<T> store = (GenericIdentifiableObjectStore<T>) getIdentifiableObjectStore( clazz );

        if ( id != null )
        {
            if ( property == null || IdentifiableProperty.ID.equals( property ) )
            {
                if ( Integer.valueOf( id ) > 0 )
                {
                    return store.get( Integer.valueOf( id ) );
                }
            }
            else if ( IdentifiableProperty.UID.equals( property ) )
            {
                return store.getByUid( id );
            }
            else if ( IdentifiableProperty.UUID.equals( property ) && OrganisationUnit.class.isAssignableFrom( clazz ) )
            {
                return (T) organisationUnitService.getOrganisationUnitByUuid( id );
            }
            else if ( IdentifiableProperty.CODE.equals( property ) )
            {
                return store.getByCode( id );
            }
            else if ( IdentifiableProperty.NAME.equals( property ) )
            {
                return store.getByName( id );
            }
            
            throw new InvalidIdentifierReferenceException( "Invalid identifiable property / class combination: " + property );
        }

        return null;
    }

    @Override
    public IdentifiableObject getObject( String uid, String simpleClassName )
    {
        for ( GenericIdentifiableObjectStore<? extends IdentifiableObject> objectStore : identifiableObjectStores )
        {
            if ( simpleClassName.equals( objectStore.getClazz().getSimpleName() ) )
            {
                return objectStore.getByUid( uid );
            }
        }

        return null;
    }

    @Override
    public IdentifiableObject getObject( int id, String simpleClassName )
    {
        for ( GenericIdentifiableObjectStore<? extends IdentifiableObject> objectStore : identifiableObjectStores )
        {
            if ( simpleClassName.equals( objectStore.getClazz().getSimpleName() ) )
            {
                return objectStore.get( id );
            }
        }

        return null;
    }

    @Override
    public void refresh( Object object )
    {
        sessionFactory.getCurrentSession().refresh( object );
    }

    @Override
    public void evict( Object object )
    {
        sessionFactory.getCurrentSession().evict( object );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> T getNoAcl( Class<T> clazz, String uid )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return null;
        }

        return (T) store.getByUidNoAcl( uid );
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> T getNoAcl( Class<T> clazz, int id )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return null;
        }

        return (T) store.getNoAcl( id );
    }

    @Override
    public <T extends IdentifiableObject> void updateNoAcl( T object )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( object.getClass() );

        if ( store != null )
        {
            store.updateNoAcl( object );
        }
    }

    @Override
    public <T extends IdentifiableObject> int getCountNoAcl( Class<T> clazz )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store != null )
        {
            return store.getCountNoAcl();
        }

        return 0;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getAllNoAcl( Class<T> clazz )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllNoAcl();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends IdentifiableObject> List<T> getBetweenNoAcl( Class<T> clazz, int first, int max )
    {
        GenericIdentifiableObjectStore<IdentifiableObject> store = getIdentifiableObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getAllNoAcl( first, max );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends DimensionalObject> List<T> getDataDimensions( Class<T> clazz )
    {
        GenericDimensionalObjectStore<DimensionalObject> store = getDimensionalObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getByDataDimension( true );
    }
    
    @Override
    @SuppressWarnings( "unchecked" )
    public <T extends DimensionalObject> List<T> getDataDimensionsNoAcl( Class<T> clazz )
    {
        GenericDimensionalObjectStore<DimensionalObject> store = getDimensionalObjectStore( clazz );

        if ( store == null )
        {
            return new ArrayList<>();
        }

        return (List<T>) store.getByDataDimensionNoAcl( true );
    }
    
    //--------------------------------------------------------------------------
    // Supportive methods
    //--------------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    private <T extends IdentifiableObject> GenericIdentifiableObjectStore<IdentifiableObject> getIdentifiableObjectStore( Class<T> clazz )
    {
        initMaps();

        GenericIdentifiableObjectStore<? extends IdentifiableObject> store = identifiableObjectStoreMap.get( clazz );

        if ( store == null )
        {
            store = identifiableObjectStoreMap.get( clazz.getSuperclass() );

            if ( store == null && !UserCredentials.class.isAssignableFrom( clazz ) )
            {
                log.warn( "No IdentifiableObjectStore found for class: " + clazz );
            }
        }

        return (GenericIdentifiableObjectStore<IdentifiableObject>) store;
    }

    @SuppressWarnings( "unchecked" )
    private <T extends NameableObject> GenericNameableObjectStore<NameableObject> getNameableObjectStore( Class<T> clazz )
    {
        initMaps();

        GenericNameableObjectStore<? extends NameableObject> store = nameableObjectStoreMap.get( clazz );

        if ( store == null )
        {
            store = nameableObjectStoreMap.get( clazz.getSuperclass() );

            if ( store == null )
            {
                log.warn( "No NameableObjectStore found for class: " + clazz );
            }
        }

        return (GenericNameableObjectStore<NameableObject>) store;
    }

    @SuppressWarnings( "unchecked" )
    private <T extends DimensionalObject> GenericDimensionalObjectStore<DimensionalObject> getDimensionalObjectStore( Class<T> clazz )
    {
        initMaps();

        GenericDimensionalObjectStore<? extends DimensionalObject> store = dimensionalObjectStoreMap.get( clazz );

        if ( store == null )
        {
            store = dimensionalObjectStoreMap.get( clazz.getSuperclass() );

            if ( store == null )
            {
                log.warn( "No DimensionalObjectStore found for class: " + clazz );
            }
        }

        return (GenericDimensionalObjectStore<DimensionalObject>) store;
    }

    private void initMaps()
    {
        if ( identifiableObjectStoreMap != null )
        {
            return; // Already initialized
        }

        identifiableObjectStoreMap = new HashMap<>();

        for ( GenericIdentifiableObjectStore<? extends IdentifiableObject> store : identifiableObjectStores )
        {
            identifiableObjectStoreMap.put( store.getClazz(), store );
        }

        nameableObjectStoreMap = new HashMap<>();

        for ( GenericNameableObjectStore<? extends NameableObject> store : nameableObjectStores )
        {
            nameableObjectStoreMap.put( store.getClazz(), store );
        }
        
        dimensionalObjectStoreMap = new HashMap<>();
        
        for ( GenericDimensionalObjectStore<? extends DimensionalObject> store : dimensionalObjectStores )
        {
            dimensionalObjectStoreMap.put( store.getClazz(), store );
        }
    }
}
