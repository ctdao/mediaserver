/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
        
package org.mobicents.media.core.resources;

import javolution.util.FastTable;

/**
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class GenericPool<E extends PoolResource> implements ResourcePool<E> {
    
    private final FastTable<E> collection;
    
    public GenericPool(int capacity) {
        this.collection = new FastTable<E>().atomic();
    }

    @Override
    public void offer(E resource) {
        // Reset state of the resource
        resource.close();
        resource.reset();
        
        // Offer object to the pool
        this.collection.add(resource);
    }

    @Override
    public E poll() {
        // Poll any object from collection
        E resource = this.collection.remove();
        
        // Initialize object for correct use
        if(resource != null) {
            resource.initialize();
        }
        return resource;
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public void release() {
        this.collection.clear();
    }

}
