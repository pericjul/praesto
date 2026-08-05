package ch.zhaw.praesto.model;

/**
 * Herkunft eines Kontos:
 * - SCHOOL: klassisches Schulmodell (Schule zahlt, Zugang via Einladung, mit Aufgaben).
 * - INDIVIDUAL: Privat-/B2C-Konto (Schüler:in meldet sich selbst an, ohne Schule/Lehrperson,
 *   ohne Aufgaben; Zugang später per Abo). null wird wie SCHOOL behandelt (Bestandskonten).
 */
public enum AccountType {
    SCHOOL,
    INDIVIDUAL
}
