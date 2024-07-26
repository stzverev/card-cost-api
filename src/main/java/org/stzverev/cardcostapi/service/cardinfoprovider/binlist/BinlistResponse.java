package org.stzverev.cardcostapi.service.cardinfoprovider.binlist;

record BinlistResponse(Number number, String scheme, String type, String brand, boolean prepaid, Country country,
                              Bank bank) {
}
