/*
 *   Copyright 2012 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License (CDDL)
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 *
 */
#pragma once

#include "common.h"
#include "RTI/encoding/BasicDataElements.h"

IEEE1516E_NS_START

/**
 * A template class that abstracts common functionality required by all underlying encoding helper
 * implementations defined by the 1516e specification macro DEFINE_ENCODING_HELPER_CLASS
 */
template <typename T> class TypeImplementation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
    
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
    private:
        T internalValue;
        T* externalPointer;
    
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
    public:
        /**
         * Constructor with initial value that will be assigned to internal memory.
         *
         * @param value The initial value to assign this type
         */
        TypeImplementation( const T& value )
        {
            setUseInternalMemory( value );
        };
    
        /**
         * Constructor for external memory. This instance changes or is changed by contents of 
         * external memory.
         * <p/>
         * Caller is responsible for ensuring that the external memory is valid for the lifetime of 
         * this object or until this object acquires new memory through setUseExternalMemory.
         * <p/>
         * A null value will construct instance to use internal memory.
         *
         * @param pointer The pointer to the  memory location that this type should read its value
         * from
         */
        TypeImplementation( T* pointer )
        {
            setUseExternalMemory( pointer );
        };
        
        virtual ~TypeImplementation()
        {
            this->externalPointer = NULL;
        };
    
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
    public:
        /**
         * Instructs the instance to use internal memory, intiialising it with the provided value.
         * <p/>
         * Subsequent calls to getValue() and setValue() will return/modify memory internal to this
         * instance.
         */
        virtual void setUseInternalMemory( const T& value )
        {
            this->externalPointer = NULL;
            this->internalValue = value;
        };
    
        /**
         * Instructs the instance to use the external memory location to read/store its value from.
         * <p/>
         * Subsequent calls to getValue() and setValue() will return/modify the data stored 
         * at the memory location specified by this function
         *
         * @param pointer the external memory location to read/store this instance's value
         */
        virtual void setUseExternalMemory( T* pointer )
        {
            this->externalPointer = pointer;
        };
    
        /**
         * Returns this instance's value.
         * <p/>
         * The value may be obtained from memory internal or external to this instance depending
         * on whether setUseInternalMemory()/setUseExternalMemory() has been called previously.
         */
        virtual T getValue()
        {
            return this->externalPointer ? *this->externalPointer : this->internalValue;
        };
    
        /**
         * Sets this instance's value.
         * <p/>
         * The value may be written to memory internal or external to this instance depending
         * on whether setUseInternalMemory()/setUseExternalMemory() has been called previously.
         */
        virtual void setValue( const T& value )
        {
            if( this->externalPointer )
                *this->externalPointer = value;
            else
                this->internalValue = value;
        };
    
    //----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
};

// As the spec itself forward declares the implementation types, we cannot simply typedef them to
// the templated TypeImplementation class above. Instead we must define the implementation type as
// a generalization of TypeImplementation and provide the appropriate constructors.
//
// The macro below has been provided to keep this process to a single line statement
#define DEFINE_TYPE_IMPL( TypeName, SimpleType )                                                \
class TypeName : public TypeImplementation<SimpleType>                                          \
{                                                                                               \
    public:                                                                                     \
        TypeName( const SimpleType& value ) : TypeImplementation<SimpleType>( value ) {};       \
        TypeName( SimpleType* pointer ) : TypeImplementation<SimpleType>( pointer ) {};         \
        virtual ~TypeName() {};                                                                 \
};

IEEE1516E_NS_END
