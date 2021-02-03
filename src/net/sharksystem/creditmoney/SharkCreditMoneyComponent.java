package net.sharksystem.creditmoney;

import net.sharksystem.ASAPFormats;
import net.sharksystem.SharkComponent;
import net.sharksystem.asap.persons.Person;

import java.util.Collection;

/**
 * This component provides a digital currency system that is based on the concept of
 * bill of exchange. I labeled it credit money because this term sound most probably more familiar and underlines
 * what this component is going to offer.
 *
 * All digital currency systems have to solve one essential problem: <b>double spending</b>. Short explanation:
 * It is no problem to create a digital version of a banknote:
 * <br/>
 * <ol>
 *     <li>find someone who is the <i>bank</i></li>
 *     <li>let this <i>bank</i> create a file (a banknote) which has a defined value (e.g. one <i>bank</i> dollar).
 *     Anything else is mandatory, e.g. a timestamp, a serial number or whatever you like to have on it.</li>
 *     <li>The <i>bank</i> signs this <i>banknote</i> and give it to others, lets say Alice.</li>
 * </ol>
 * <br/>
 * Here is the problem: ALice cannot falsify another banknote. She has not got <i>banks</i> private key and cannot sign
 * on its behalf. But she does not has to. She can make copies. She can give one to Bob, another to Clara and so forth.
 * Solve this double spending problem and you have a digitial currency. There are two well-known approaches:
 * <br/>
 * <ul>
 *     <li><b>centralized systems:</b> The <i>bank</i> keeps always track on the ownership of their banknotes.
 *     Alice would not simply give a banknote to Bob but would tell as well. Her attempt to give same banknote to
 *     Clara would fail. Clara would find out that Alice is no longer in possession of this banknote.</li>
 *     <li><b>blockchains</b> A P2P networks keeps track of all transactions. The central <i>bank</i> entity is replaced
 *     by a P2P system</li>
 * </ul>
 * <br/>
 * Here is the interesting part: Banknotes are not invented from thin air. They are derived from an older concept:
 * bill of exchange which dates back to the 13th century and can be seen as the time of birth of finance capitalism
 * (no matter if you like that system or not).
 *
 * It is quite simple as most revolutionary ideas. Bills of exchange became a substitute of coined money. Coined money
 * has its value in itself. E.g. a coin made of gold is considered to be of value. (Don't ask me why. Robinson Crusoe could use
 * nearly anything from his wreckage but the gold. That's another story, though.) Other examples: A big fat original
 * Rolex, some pieces of art.
 *
 * Things that have the value in itself can easily be stolen. That was a problem in those time and it still is.
 * Merchants knew others in other parts of the world, though. Now, Alice might plan a trip from A to B but does not like
 * the idea of taking to much coins with her. A merchant offers in her city offers a deal: She would give him her coins
 * and he would produce a document. This document would order / ask his business partner in city B to give Alice (and
 * only Alice) the same amount of coins. (He would take a fee for this service but that is less relevant.)
 * <br/>
 * Alice would travel with a personalized piece of paper which has only value to herself instead of coins. (This
 * concept is still in use e.g. under the term of Hawala.)
 * <br/>
 * <br/>
 * We adopt that concept and create a decentralized credit money systems that works fine within closed user groups.
 * Groups can be as huge as you like. Users have to exchange digital certificates (we use
 * <a href="https://github.com/SharedKnowledge/ASAPCertificateExchange/wiki">SharkCertificateExchange</a> in this library).
 * It works without a central <i>bank</i> but also without a P2P system like blockchain. It requires nothing but a
 * digitial device but not even Internet access.
 * <br/>
 * <br/>
 * At first, there are just two parties involved. A <b>debtor</b> and a <b>creditor</b> sign a <b>bond</b>. This bond
 * contains
 * <br/>
 * <ul>
 *     <li>An integer number of debtor choice. It illustrates the amount s/he owes. This number could represent
 *     another currency, like US Dollar, Euro, Bitcoin</li>
 *     <li>Signature of both debtor and creditor: They sign and herewith agree to this bond.</li>
 *     <li>Expiration data. It is also an implication of a digital certificate. It has a expiration data and so any
 *     digital signature</li>
 * </ul>
 * <br/>
 * Why would a debtor sign this bond? S/he could really have got sometime of value and ows the debtor. This bond
 * would explicitly declare that fact.
 * <br/>
 * <br/>
 * A banknote is a special bond.
 * There are some special <i>banks</i> in the financial system which were granted the right to produce banknotes, see
 * <i>central banks</i>. In same currencies was (or even is?) even a statement like:
 * "Give the bearer of this note" e.g. some amount of money. The US dollar banknote was over a period of time a
 * symbol for a defined amount of gold that was stored in a high secure treasury. The US national bank was debtor of
 * banknote owners. In theory, the had to give gold in exchange for a banknote. This exchange was and is not
 * useful, though. The trust in a <i>central bank</i> makes it banknote as valuable as the actual gold.
 *
 * This gold binding ceased in the 1970th. US dollars bills are still in use. It's all about trust. An interesting
 * point we will investigate very soon.
 * <br/>
 * <br/>
 * Bonds can be chained in our system (not, this will not become a blockchain!).
 * Let's say: Bob ows Alice something and they have created a bond. Let's also assume that Clara
 * gets something from Bob and ows him. Clara and Bob could create a new bond.
 * <br/>
 * Bob could also ask Clara to cover for his depth to Alice in the bond. There are several questions to be answered:
 * <ul>
 *     <li>Would Alice like it? Would a debtor accept a transfer of the creditor?</li>
 *     <li>Would Clara like it? Would a creator like to become in fact debtor of a third person?</li>
 *     <li>Why would Bob propose it? What is in for him?</li>
 * </ul>
 * <br/>
 * The answers can be yes or no, depending on your requirements. Clara could refuse. What would her transaction with
 * Bob had to do with Alice? On the other hand side: The fact that Bob ows Alice illustrates the trust of Alice
 * in Bob to stand for its promise to pay back. Furthermore, maybe Clara prefers to owe Alice something instead of Bob
 * for whatever reasons.
 * <br/>
 * Bob could refuse. He does not want Alice to know about its deal with Clara. Bob could like expanding the bond-chain
 * because he does not like to go after his debtors.
 * <br/>
 * Alice could refuse. She had a contract with Bob and could have no interest in a replacement by anybody else.
 * Alice could also like the idea. She would become a variant of a central bank. How so? Imagine Alice a very
 * trustworthy person. Imagine her a person who would pay back immediately if asked. In that case, a creditor of
 * Alice would have got something that is very similar to a banknote.
 * <br/>
 * Replace Alice with a more general concept like a <i>group</i>. You application could introduce a concept group
 * maybe represented by a person. (Have a look of e.g. the British Pound. There she is, the representative of the
 * trust in the British banknote, the queen herself.)
 * <br/>
 * This <i>group</i> could issue bonds. Receivers became creditors of that <i>group</i>. The <i>group</i> would
 * literally sign its trust in the creditor. Persons who trust the group would gladly accept such a bond and became
 * creditors of the group. In that case, a credit based currency is born. It would only be valid for people who
 * trust the group.
 * <br/>
 * What could such a group be? That's up to you. It could be a living community who want a fair share of housework.
 * It could a very libertarian community who refuses money in general. It could also be a
 * "Goodfather" like group. This library offers a system of formalized to disseminate of depths within a closed
 * user group. It can be used and understood as a banknote but also anything else.
 * <br/>
 * A bond ceases to exist after a while. That is an interesting feature. Bonds must be re-established after a while or
 * get lost. Another striking similarity to all paper based currencies ever existed.
 */
@ASAPFormats(formats = {SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT})
public interface SharkCreditMoneyComponent extends SharkComponent {
    String SHARK_CREDIT_MONEY_FORMAT = "shark/money";
    String SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI = "sharkMoney://signAsDebtor";
    String SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI = "sharkMoney://signAsCreditor";
    String SHARK_CREDIT_MONEY_SIGNED_BOND_URI = "sharkMoney://signedBond";
    String SHARK_CREDIT_MONEY_ANNUL_BOND_URI = "sharkMoney://annulBond";

    /**
     * Create a bond. It is a decentralized system. Bond creation requires interaction of several peers. This
     * method will not return anything. A listener
     * @throws
     */
    void createBond(Person creditor, Person debtor, CharSequence unit, int amount)
            throws SharkCreditMoneyException;

    Collection<SharkCreditBond> getBondsByCreditor(Person creditor);

    Collection<SharkCreditBond> getBondsByDebtor(Person debtor);

    Collection<SharkCreditBond> getBondsByCreditorAndDebtor(Person creditor, Person debtor);

    void replaceDebtor(SharkCreditBond bond, Person newDebtor) throws SharkCreditMoneyException;

    void replaceCreditor(SharkCreditBond bond, Person newCreditor) throws SharkCreditMoneyException;

    void subscribeSharkCreditBondReceivedListener(SharkCreditBondReceivedListener listener);

    void annulBond(SharkCreditBond bond) throws SharkCreditMoneyException;

}
