package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;


    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // Get the next available slot number from the database
        int nextAvailableSlotBefore = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        // Check the database for an existing ticket for this vehicle
        // There should not be one yet, so this should be null
        Ticket ticketBefore = ticketDAO.getTicket("ABCDEF");

        // Process the vehicle
        // This should cause the next available slot number to change, and a ticket to be created
        parkingService.processIncomingVehicle();

        // Get the next available slot number from the database again, it should be different now
        int nextAvailableSlotAfter = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        // Check the database again for this vehicle's ticket
        // It should now exist
        Ticket ticketAfter = ticketDAO.getTicket("ABCDEF");

        // Check that Parking table is updated by confirming next available slot has changed
        assertNotEquals(nextAvailableSlotAfter, nextAvailableSlotBefore);
        // Check that ticket has been created by confirming ticket exists for test registration number
        // Also confirm that it did not exist prior to running test, so we can be certain it was created by our test
        assertNull(ticketBefore);
        assertNotNull(ticketAfter);
    }

    @Test
    public void testParkingLotExit(){

        // Create our ParkingService, and park a vehicle so that we are ready to test its exit
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        // Get the parked vehicle's ticket from the database
        Ticket ticketBefore = ticketDAO.getTicket("ABCDEF");

        // Update the ticket, setting the inTime to 2 hours ago for testing purposes
        ticketBefore.setInTime(LocalDateTime.now().minusHours(2));
        ticketDAO.saveTicket(ticketBefore);

        // Process the vehicle exit, and get the updated ticket from the database
        parkingService.processExitingVehicle();
        Ticket ticketAfter = ticketDAO.getTicket("ABCDEF");

        // Check that fare has been generated and stored in the database
        // by comparing the ticket's price before and after processing
        assertNotEquals(ticketAfter.getPrice(), ticketBefore.getPrice());
        // Check that ticket now has an out time stored
        assertNotNull(ticketAfter.getOutTime());
    }

}
