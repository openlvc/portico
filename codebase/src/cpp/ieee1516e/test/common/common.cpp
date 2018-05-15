/*
*   Copyright 2007 The Portico Project
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
#include "common.h"

/*
* Fail the current test with the given message
*/
void failTest(const char *format, ...)
{
	// start the var-arg stuff
	va_list args;
	va_start(args, format);

	// turn the args into a single string
	// http://www.cplusplus.com/reference/clibrary/cstdio/vsprintf.html
	char buffer[4096];
	vsprintf(buffer, format, args);

	// clean up the varargs
	va_end(args);

	// kill the test
	CPPUNIT_FAIL(buffer);
}

/*
* Test should fail because an exception was expected, but none occurred. The failure message
* will also include the action that was underway (and should have caused an exception).
*/
void failTestMissingException(const char *expectedException, const char* action)
{
	char buffer[4096];
	sprintf(buffer,
		"(missingException) Expected an exception (%s) while %s",
		expectedException,
		action);
	CPPUNIT_FAIL(buffer);
}

/*
* An exception was received, but it wasn't the one we expected. The failure message will
* include the expected and actual exception types and a message regarding the action that
* was in progress.
*/
void failTestWrongException(const char *expected, rti1516e::Exception &actual, const char *action)
{
	char buffer[4096];

	// NOTE actual.what() is a wide string and will need to be converted to a narrow wstring
	// to be used

	sprintf(buffer,
		"(wrongException) Wrong exception received while %s: expected [%s], received[%s]\n",
		action,
		expected,
		"FIXME");
	CPPUNIT_FAIL(buffer);
}
