package me.zodac.folding.api.ejb;


/**
 * In order to decouple both the REST layer and the {@link BusinessLogic} from the persistence solution we use this
 * interface - to be implemented as an EJB - as a single point of entry for CRUD operations.
 * Since some of the persisted data can be cached, we don't want any other modules of the
 * codebase to need to worry about DB vs cache access, and instead encapsulate all of that logic here.
 */
public interface Storage {

//    public Hardware createHardware(final Hardware hardware) throws FoldingException
//
//    public Collection<Hardware> getAllHardware() throws FoldingException
//
//    public Optional<Hardware> getHardwareForUser(final User user)
//
//    public void updateHardware(final Hardware updatedHardware) throws FoldingException, HardwareNotFoundException, FoldingExternalServiceException
//
//    public void deleteHardware(final int hardwareId) throws FoldingException
//
//    public User createUser(final User user) throws FoldingException, FoldingExternalServiceException
//
//    public User getUser(final int userId) throws FoldingException, UserNotFoundException
//
//    public User getUserWithPasskey(final int userId, final boolean showFullPasskeys) throws FoldingException, UserNotFoundException
//
//    public Collection<User> getAllUsers() throws FoldingException
//
//    public Collection<User> getAllUsersWithPasskeys(final boolean showFullPasskeys) throws FoldingException
//
//    public void updateUser(final User updatedUser) throws FoldingException, UserNotFoundException, FoldingExternalServiceException
//
//    public void deleteUser(final int userId) throws FoldingException
//
//    public Team createTeam(final Team team) throws FoldingException
//
//    public Team getTeam(final int teamId) throws FoldingException, TeamNotFoundException
//
//    public Collection<Team> getAllTeams() throws FoldingException
//
//    public void updateTeam(final Team team) throws FoldingException
//
//    public void deleteTeam(final int teamId) throws FoldingException
//
//    public boolean doesNotContainHardware(final int hardwareId)
//
//    public boolean doesNotContainTeam(final int teamId)
//
//    public Collection<User> getUsersOnTeam(final Team team) throws FoldingException
//
//    public Optional<Hardware> getHardwareWithName(final String hardwareName)
//
//    public Optional<Team> getTeamWithName(final String teamName)
//
//    public Optional<User> getUserWithFoldingUserNameAndPasskey(final String foldingUserName, final String passkey)
//
//    public Optional<User> getUserWithHardware(final Hardware hardware)
//
//    public Optional<User> getUserWithTeam(final Team team)
//
//    public void persistInitialUserStats(final User user) throws FoldingException, FoldingExternalServiceException
//
//    public void persistInitialUserStats(final UserStats userStats) throws FoldingException
//
//    public Stats getInitialStatsForUser(final int userId) throws FoldingException
//
//    public void persistHourlyTcStatsForUser(final UserTcStats userTcStats) throws FoldingException
//
//    public UserTcStats getTcStatsForUser(final int userId) throws UserNotFoundException, FoldingException, NoStatsAvailableException
//
//    public Collection<HistoricStats> getHistoricStatsHourly(final int userId, final int day, final Month month, final Year year) throws FoldingException, UserNotFoundException
//
//    public Collection<HistoricStats> getHistoricStatsDaily(final int userId, final Month month, final Year year) throws FoldingException, UserNotFoundException
//
//    public Collection<HistoricStats> getHistoricStatsMonthly(final int userId, final Year year) throws FoldingException
//
//    public void addOffsetStats(final int userId, final OffsetStats offsetStats) throws FoldingException
//
//    public void addOrUpdateOffsetStats(final int userId, final OffsetStats offsetStats) throws FoldingException
//
//    public OffsetStats getOffsetStatsForUser(final int userId) throws FoldingException
//
//    public void initialiseOffsetStats() throws FoldingException
//
//    public void clearOffsetStats() throws FoldingException
//
//    public void persistTotalStatsForUser(final UserStats stats) throws FoldingException
//
//    public Stats getTotalStatsForUser(final int userId) throws FoldingException
//
//    public void updateInitialStatsForUser(final User user) throws UserNotFoundException, FoldingException
//
//    public SystemUserAuthentication authenticateSystemUser(final String userName, final String password) throws FoldingException
//
//    public Collection<RetiredUserTcStats> getRetiredUsersForTeam(final Team team) throws FoldingException
//
//    public void deleteRetiredUserStats() throws FoldingException
}
