package com.lordofthejars.nosqlunit.core;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.anyString;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class WhenRefreshOperationIsExecuted {

	@Mock private DatabaseOperation databaseOperation;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void insert_operations_should_be_executed() {
		
		RefreshLoadStrategyOperation refreshLoadStrategyOperation = new RefreshLoadStrategyOperation(databaseOperation);
		String[] contents = new String[]{"My name is","Jimmy Pop"};
		
		refreshLoadStrategyOperation.executeScripts(contents);
		verify(databaseOperation, times(2)).insertNotPresent(anyString());
		
	}

	@Test(expected=IllegalArgumentException.class)
	public void refresh_operation_should_throw_an_exception_is_no_data_available() {
		
		RefreshLoadStrategyOperation refreshLoadStrategyOperation = new RefreshLoadStrategyOperation(databaseOperation);
		String[] contents = new String[]{};
		refreshLoadStrategyOperation.executeScripts(contents);
	}
	
}
