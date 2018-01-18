package fi.om.initiative.conf.saml;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import fi.om.initiative.dto.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;


public class SamlUserLoader implements SAMLUserDetailsService {

    public static final String FINNISH_CITIZEN_FLAG = "1";

    protected static final String MISSING_MUNICIPALITY_FI = "Kotikunta ei tiedossa";
    protected static final String MISSING_MUNICIPALITY_SV = "Hemkommun saknas";

    @Override
    public SamlUser loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {

        /**
         * Test-environment response with nordea details. name | friendlyname | value
         http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName | firstName | Portaalia Nordea
         urn:oid:1.2.246.517.2002.2.7 | VakinainenKotimainenLahiosoitePostitoimipaikkaS | Helsinki
         urn:oid:2.16.840.1.113730.3.1.241 | displayName | Nordea
         urn:oid:1.2.246.517.3002.111.2 | null | true
         urn:oid:1.2.246.517.2002.2.6 | VakinainenKotimainenLahiosoitePostinumero | 00120
         urn:oid:2.5.4.42 | givenName | Nordea
         urn:oid:1.2.246.517.2002.2.19 | KotikuntaKuntaS | Helsinki
         urn:oid:2.5.4.3 | cn | Testaaja Portaalia Nordea
         urn:oid:2.5.4.4 | sn | Testaaja
         urn:oid:1.2.246.517.2002.2.18 | KotikuntaKuntanumero | 019
         urn:oid:1.2.246.517.2002.2.4 | VakinainenKotimainenLahiosoiteS | Nordeatie 2
         urn:oid:1.2.246.21 | nationalIdentificationNumber | 210281-9988
         */

        String municipalityNameFi = firstNotEmpty(
                credential.getAttributeAsString("urn:oid:1.2.246.517.2002.2.19") // Finnish city
        ).orElse(MISSING_MUNICIPALITY_FI);
        String municipalityNameSv = firstNotEmpty(
                credential.getAttributeAsString("urn:oid:1.2.246.517.2002.2.20"), // Swedish city
                credential.getAttributeAsString("urn:oid:1.2.246.517.2002.2.19") // Finnish city
        ).orElse(MISSING_MUNICIPALITY_SV);

        String firstNames = credential.getAttributeAsString("http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName");
        String lastName = credential.getAttributeAsString("urn:oid:2.5.4.4");

        String ssn = credential.getAttributeAsString("urn:oid:1.2.246.21");

        String finnishCitizen = credential.getAttributeAsString("urn:oid:1.2.246.517.2002.2.26");

        String address = "";

        return new SamlUser(User.validateSSN(ssn), address, firstNames, lastName, municipalityNameFi, municipalityNameSv, FINNISH_CITIZEN_FLAG.equals(finnishCitizen));
    }

    static Optional<String> firstNotEmpty(String ... values) {
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .filter(v -> v.trim().length() != 0)
                .findFirst();
    }
}
