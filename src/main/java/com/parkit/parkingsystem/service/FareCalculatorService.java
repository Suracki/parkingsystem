package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.time.Duration;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        // If the ticket does not have an outTime, or the outTime is before the inTime, throw an exception
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().isBefore(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        // Calculate the duration between the inTime and outTIme in hours (or partial hours)
        double duration = (Duration.between(ticket.getInTime(), ticket.getOutTime()).toMinutes()) / 60.0;

        // If the duration is under 30 minutes, apply the free parking discount
        if (duration <= 0.5){
            ticket.setPrice(0);
        }

        // Otherwise, check what vehicle type the ticket is for
        else {
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    // If the vehicle has multiple visits, apply 5% discount and set price
                    if (ticket.getVisits() > 1) {
                        double price = Math.round(duration * Fare.CAR_RATE_PER_HOUR * 95);
                        price = price / 100;
                        ticket.setPrice(price);
                    }
                    // Otherwise, set full price
                    else {
                        ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                    }
                    break;
                }
                case BIKE: {
                    // If the vehicle has multiple visits, apply 5% discount and set price
                    if (ticket.getVisits() > 1) {
                        double price = Math.round(duration * Fare.BIKE_RATE_PER_HOUR * 95);
                        price = price / 100;
                        ticket.setPrice(price);
                    }
                    // Otherwise, set full price
                    else {
                        ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                    }
                    break;
                }
                // If the vehicle is neither a CAR or a BIKE, throw exception
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }
}