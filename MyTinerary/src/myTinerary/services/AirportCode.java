package myTinerary.services;

// Represents major commercial airports in the US
// http://www.world-airport-codes.com/world-top-30-airports.html
//ToDo: Use @Override + toString for friendly name (Seattle) in UI?
// One problem with using an enum is we have to cover all the start/end points
// for flights or we get an exception converting to this enum.
public enum AirportCode {

	SEA,
	LAX,
	SFO,
	ATL, // Hartsfield Jackson Atlanta International
	CDG, // Charles de Gaulle International
	CGK, // Soekarno-Hatta International
	DFW, // Dallas Fort Worth International
	DXB, // Dubai International
	HND, // Tokyo International
	LHR, // London Heathrow
	PEK, // Beijing Capital International
	ORD, // Chicago O'Hare International
	LGA,
	SAN
}

