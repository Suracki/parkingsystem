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

import java.time.LocalDateTime;

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

        System.out.println("INT TEST 1");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        int nextAvailableSlotBefore = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        Ticket ticketBefore = ticketDAO.getTicket("ABCDEF");
        parkingService.processIncomingVehicle();
        int nextAvailableSlotAfter = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        Ticket ticketAfter = ticketDAO.getTicket("ABCDEF");

        // Check that Parking table is updated by confirming next available slot has changed
        assertNotEquals(nextAvailableSlotAfter, nextAvailableSlotBefore);
        // Check that ticket has been created by confirming ticket exists for test registration number
        // Also confirm that it did not exist prior to running test
        assertNull(ticketBefore);
        assertNotNull(ticketAfter);
    }

    @Test
    public void testParkingLotExit(){

        System.out.println("INT TEST 2");
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        try {
            Thread.sleep(20);
        }
        catch (Exception e) {

        }
        parkingService.processExitingVehicle();

        double price = 999;

        LocalDateTime outTime = null;

        Ticket ticket = ticketDAO.getTicket("ABCDEF");



        price = ticket.getPrice();
        outTime = ticket.getOutTime();


        // Check that fare has been generated and stored in the database
        assertNotEquals(price, 999);
        // Check that ticket has an out time stored
        assertNotNull(outTime);
    }

}
