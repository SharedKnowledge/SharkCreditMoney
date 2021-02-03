package net.sharksystem.creditmoney;

import net.sharksystem.ASAPFormats;

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
 * Why would a debtor sign this bond? S/he could really have got sometime of value and is going to pay back. This bond
 * would be a declaration depths which is meant to be payed back.
 * <br/>
 * <br/>
 * Debtor could also act as a kind of <i>central bank</i>. There are some special <i>banks</i> in the financial system
 * which were granted the right to produce banknotes. Those banknotes are provided to usual banks which give it to
 * ordinary people. Banknotes are in fact a variant of our bonds. This bond is special because the initial creditor
 * is a very special institute. An institute the people trust. We come back to this point very soon.
 * <br/>
 * <br/>
 * Bonds can be transferred in our system. [TODO: continue]
 *
 * [The <i>central bank</i> becomes creditor of the <i>bank</i>. A <i>bank</i> wants those bond because
 * the are...]
 *
 *
 */
@ASAPFormats(formats = {SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_FORMAT})
public interface SharkCreditMoneyComponent {
    String SHARK_CREDIT_MONEY_FORMAT = "shark/money";


}
